package com.example.extension

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import com.example.data.ExtensionEntity
import com.example.data.LaneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ExtensionManager(
    private val context: Context,
    private val repository: LaneRepository
) {
    private val extensionsDir: File = File(context.filesDir, "installed_extensions")

    init {
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs()
        }
    }

    /**
     * Seeds built-in extensions into filesystem and Room database if not yet present.
     */
    suspend fun initializeExtensions() = withContext(Dispatchers.IO) {
        try {
            BuiltInExtensions.seedDefaults(extensionsDir)

            val defaults = listOf(
                ExtensionEntity(
                    id = "captcha_solver",
                    name = "Auto Captcha Solver",
                    version = "1.4.0",
                    description = "Detects and solves reCAPTCHA, hCaptcha & Cloudflare challenges in real time.",
                    enabledGlobal = true,
                    isBuiltIn = true,
                    manifestJson = BuiltInExtensions.CAPTCHA_SOLVER_MANIFEST,
                    extractedDirPath = File(extensionsDir, "captcha_solver").absolutePath
                ),
                ExtensionEntity(
                    id = "insta_2fa",
                    name = "Insta Auto 2FA & Auth Assistant",
                    version = "2.1.0",
                    description = "Autofills two-factor security codes and assists Instagram login challenges.",
                    enabledGlobal = true,
                    isBuiltIn = true,
                    manifestJson = BuiltInExtensions.INSTA_2FA_MANIFEST,
                    extractedDirPath = File(extensionsDir, "insta_2fa").absolutePath
                ),
                ExtensionEntity(
                    id = "ad_shield",
                    name = "Ad & Tracker Shield",
                    version = "3.0.0",
                    description = "Eliminates intrusive advertisements and tracking scripts across web pages.",
                    enabledGlobal = true,
                    isBuiltIn = true,
                    manifestJson = BuiltInExtensions.AD_SHIELD_MANIFEST,
                    extractedDirPath = File(extensionsDir, "ad_shield").absolutePath
                ),
                ExtensionEntity(
                    id = "dark_reader",
                    name = "Dark Reader Mode",
                    version = "1.2.0",
                    description = "Injects high-contrast dark themes on any website for seamless night viewing.",
                    enabledGlobal = true,
                    isBuiltIn = true,
                    manifestJson = BuiltInExtensions.DARK_READER_MANIFEST,
                    extractedDirPath = File(extensionsDir, "dark_reader").absolutePath
                )
            )

            for (ext in defaults) {
                repository.insertExtension(ext)
            }
        } catch (e: Exception) {
            Log.e("ExtensionManager", "Error seeding built-in extensions: ${e.message}", e)
        }
    }

    /**
     * Extracts a user-uploaded ZIP containing manifest.json and extension assets.
     * Prevents Zip Slip vulnerability using canonical path verification.
     */
    suspend fun installExtensionFromZip(zipUri: Uri): Result<ExtensionEntity> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(zipUri)
                ?: return@withContext Result.failure(Exception("Cannot open ZIP uri"))

            val extId = "ext_" + UUID.randomUUID().toString().take(8)
            val targetDir = File(extensionsDir, extId)
            if (!targetDir.exists()) targetDir.mkdirs()

            unzipWithZipSlipProtection(inputStream, targetDir)

            // Look for manifest.json (could be at root or inside a single top-level folder)
            var manifestFile = File(targetDir, "manifest.json")
            var effectiveDir = targetDir

            if (!manifestFile.exists()) {
                val subdirs = targetDir.listFiles { f -> f.isDirectory }
                if (subdirs != null && subdirs.size == 1) {
                    val candidate = File(subdirs[0], "manifest.json")
                    if (candidate.exists()) {
                        manifestFile = candidate
                        effectiveDir = subdirs[0]
                    }
                }
            }

            if (!manifestFile.exists()) {
                targetDir.deleteRecursively()
                return@withContext Result.failure(Exception("Invalid extension ZIP: manifest.json not found."))
            }

            val manifestContent = manifestFile.readText()
            val parsed = ExtensionManifest.fromJson(manifestContent)

            val entity = ExtensionEntity(
                id = extId,
                name = parsed.name.ifBlank { "Custom Extension" },
                version = parsed.version.ifBlank { "1.0.0" },
                description = parsed.description,
                enabledGlobal = true,
                isBuiltIn = false,
                manifestJson = manifestContent,
                extractedDirPath = effectiveDir.absolutePath
            )

            repository.insertExtension(entity)
            repository.addLog(0, "SUCCESS", "Installed Chrome Extension: ${entity.name} v${entity.version}")
            Result.success(entity)
        } catch (e: Exception) {
            Log.e("ExtensionManager", "Failed to extract ZIP: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Secure ZIP extraction preventing Directory Traversal (Zip Slip).
     */
    private fun unzipWithZipSlipProtection(zipInputStream: InputStream, destDir: File) {
        val canonicalDestDirPath = destDir.canonicalPath
        ZipInputStream(BufferedInputStream(zipInputStream)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val newFile = File(destDir, entry.name)
                val canonicalNewFilePath = newFile.canonicalPath

                // Strict Zip Slip defense: Ensure target path remains within destDir
                if (!canonicalNewFilePath.startsWith(canonicalDestDirPath + File.separator) &&
                    canonicalNewFilePath != canonicalDestDirPath
                ) {
                    throw SecurityException("Zip Slip path traversal attempt blocked: ${entry.name}")
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        val buffer = ByteArray(4096)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Builds and injects extension scripts and CSS into the WebView for active extensions.
     */
    suspend fun injectExtensions(
        webView: WebView,
        laneId: Int,
        extensionIds: List<String>,
        currentUrl: String,
        runAtStage: String // "document_start" or "document_end"
    ) = withContext(Dispatchers.IO) {
        if (extensionIds.isEmpty() || currentUrl.isBlank() || currentUrl == "about:blank") return@withContext

        val scriptsToInject = mutableListOf<String>()

        for (extId in extensionIds) {
            val extDir = File(extensionsDir, extId)
            val manifestFile = File(extDir, "manifest.json")
            if (!manifestFile.exists()) continue

            try {
                val manifest = ExtensionManifest.fromJson(manifestFile.readText())

                for (cs in manifest.contentScripts) {
                    // Check if runAt matches (or default)
                    val runAt = cs.runAt.lowercase()
                    val stageMatches = when (runAtStage) {
                        "document_start" -> runAt == "document_start"
                        else -> runAt != "document_start" // document_end or document_idle
                    }

                    if (!stageMatches) continue

                    // Check URL pattern matching
                    if (!matchesUrl(cs.matches, currentUrl)) continue

                    // 1. Inject CSS if present
                    for (cssPath in cs.cssFiles) {
                        val cssFile = File(extDir, cssPath)
                        if (cssFile.exists()) {
                            val cssText = cssFile.readText()
                            val escapedCss = escapeJsString(cssText)
                            val injectCssJs = """
                                (function() {
                                    try {
                                        var style = document.createElement('style');
                                        style.setAttribute('data-extension-id', '$extId');
                                        style.textContent = '$escapedCss';
                                        (document.head || document.documentElement).appendChild(style);
                                    } catch(e) { console.error('CSS injection error:', e); }
                                })();
                            """.trimIndent()
                            scriptsToInject.add(injectCssJs)
                        }
                    }

                    // 2. Inject Content JS if present
                    for (jsPath in cs.jsFiles) {
                        val jsFile = File(extDir, jsPath)
                        if (jsFile.exists()) {
                            val jsCode = jsFile.readText()
                            val wrapped = wrapWithChromePolyfill(extId, laneId, jsCode)
                            scriptsToInject.add(wrapped)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ExtensionManager", "Error preparing extension $extId: ${e.message}", e)
            }
        }

        if (scriptsToInject.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                for (code in scriptsToInject) {
                    webView.evaluateJavascript(code, null)
                }
            }
        }
    }

    /**
     * Wraps raw extension JS code in the Chrome Extension runtime polyfill,
     * providing `chrome.runtime`, `chrome.storage.local`, and bridge hooks.
     */
    private fun wrapWithChromePolyfill(extensionId: String, laneId: Int, rawCode: String): String {
        return """
        (function() {
            try {
                window.__current_lane_id__ = $laneId;
                if (!window.chrome) { window.chrome = {}; }
                if (!window.chrome.runtime) {
                    window.chrome.runtime = {
                        id: "$extensionId",
                        sendMessage: function(msg, callback) {
                            if (window.__android_lane_bridge__) {
                                window.__android_lane_bridge__.onExtensionMessage("$extensionId", JSON.stringify(msg));
                            }
                            if (callback) callback({ status: "ok" });
                        }
                    };
                }
                if (!window.chrome.storage) {
                    window.chrome.storage = {
                        local: {
                            get: function(key, cb) {
                                var res = {};
                                if (window.__android_lane_bridge__) {
                                    var val = window.__android_lane_bridge__.getStorage(key);
                                    if (val) {
                                        try { res[key] = JSON.parse(val); } catch(e) { res[key] = val; }
                                    }
                                }
                                if (cb) cb(res);
                            },
                            set: function(obj, cb) {
                                if (window.__android_lane_bridge__) {
                                    for (var k in obj) {
                                        window.__android_lane_bridge__.setStorage(k, JSON.stringify(obj[k]));
                                    }
                                }
                                if (cb) cb();
                            }
                        }
                    };
                }
                $rawCode
            } catch(extErr) {
                console.error("[ChromeExt:$extensionId] Runtime error: ", extErr);
            }
        })();
        """.trimIndent()
    }

    private fun escapeJsString(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    private fun matchesUrl(patterns: List<String>, url: String): Boolean {
        if (patterns.isEmpty()) return true
        for (pattern in patterns) {
            if (pattern == "<all_urls>" || pattern == "*://*/*") return true
            try {
                val regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .toRegex()
                if (regex.containsMatchIn(url)) return true
            } catch (_: Exception) {
                if (url.contains(pattern.replace("*", ""))) return true
            }
        }
        return false
    }
}
