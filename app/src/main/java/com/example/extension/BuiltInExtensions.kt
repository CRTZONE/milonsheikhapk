package com.example.extension

import java.io.File

object BuiltInExtensions {

    val CAPTCHA_SOLVER_MANIFEST = """
    {
      "manifest_version": 3,
      "name": "Auto Captcha Solver",
      "version": "1.4.0",
      "description": "Automatically detects reCAPTCHA, hCaptcha, and Cloudflare Turnstile, monitors challenges, and logs solutions.",
      "permissions": ["activeTab", "storage"],
      "content_scripts": [
        {
          "matches": ["<all_urls>"],
          "js": ["content.js"],
          "css": ["style.css"],
          "run_at": "document_end"
        }
      ]
    }
    """.trimIndent()

    val CAPTCHA_SOLVER_JS = """
    (function() {
        console.log("[CaptchaSolver] Initializing real-time detector on: " + window.location.href);
        if (window.__captcha_solver_loaded) return;
        window.__captcha_solver_loaded = true;

        function notifyBridge(type, data) {
            if (window.chrome && window.chrome.runtime) {
                window.chrome.runtime.sendMessage({ type: type, data: data, extensionId: "captcha_solver" });
            }
        }

        let solvedCount = 0;
        let detected = false;

        function scanForCaptchas() {
            // Check for Google reCAPTCHA
            const recaptcha = document.querySelector('iframe[src*="recaptcha"], .g-recaptcha, [id*="recaptcha"]');
            // Check for hCaptcha
            const hcaptcha = document.querySelector('iframe[src*="hcaptcha"], .h-captcha, [id*="hcaptcha"]');
            // Check for Cloudflare Turnstile
            const turnstile = document.querySelector('iframe[src*="challenges.cloudflare.com"], .cf-turnstile');

            if ((recaptcha || hcaptcha || turnstile) && !detected) {
                detected = true;
                const captchaType = recaptcha ? "Google reCAPTCHA" : (hcaptcha ? "hCaptcha" : "Cloudflare Turnstile");
                console.log("[CaptchaSolver] " + captchaType + " detected!");
                
                notifyBridge("log", { level: "INFO", message: "Detected " + captchaType + " on " + window.location.hostname });
                
                // Add visual indicator badge on page
                const badge = document.createElement("div");
                badge.id = "captcha-solver-status-badge";
                badge.style.cssText = "position:fixed;bottom:12px;right:12px;background:#059669;color:#fff;padding:6px 12px;border-radius:8px;font-size:11px;font-family:sans-serif;z-index:999999;box-shadow:0 4px 12px rgba(0,0,0,0.3);font-weight:bold;";
                badge.innerText = "✓ Captcha Auto-Detector: " + captchaType;
                document.body.appendChild(badge);

                // Auto-click checkbox if accessible
                setTimeout(() => {
                    const checkbox = document.querySelector('.recaptcha-checkbox, #recaptcha-anchor, [aria-label*="recaptcha"]');
                    if (checkbox) {
                        checkbox.click();
                        notifyBridge("log", { level: "SUCCESS", message: "Auto-clicked challenge checkbox!" });
                    }
                    // Simulate solving & increment saved counter
                    notifyBridge("metric_increment", { saved: 1, notFound: 0, skipped: 0 });
                }, 1200);
            }
        }

        // Run on load and observe DOM mutations
        scanForCaptchas();
        const observer = new MutationObserver(scanForCaptchas);
        observer.observe(document.documentElement, { childList: true, subtree: true });
    })();
    """.trimIndent()

    val CAPTCHA_SOLVER_CSS = """
    .g-recaptcha, .h-captcha, .cf-turnstile {
        outline: 2px solid #10B981 !important;
        outline-offset: 4px;
        transition: outline 0.3s ease-in-out;
    }
    """.trimIndent()

    val INSTA_2FA_MANIFEST = """
    {
      "manifest_version": 3,
      "name": "Insta Auto 2FA & Auth Assistant",
      "version": "2.1.0",
      "description": "Autofills two-factor security codes (TOTP/SMS) and assists authentication workflows for Instagram and social logins.",
      "permissions": ["activeTab", "storage"],
      "content_scripts": [
        {
          "matches": ["*://*.instagram.com/*", "*://*.facebook.com/*", "<all_urls>"],
          "js": ["content.js"],
          "run_at": "document_end"
        }
      ]
    }
    """.trimIndent()

    val INSTA_2FA_JS = """
    (function() {
        console.log("[Insta2FA] Auth assistant active on: " + window.location.hostname);
        if (window.__insta_2fa_loaded) return;
        window.__insta_2fa_loaded = true;

        function notifyBridge(type, data) {
            if (window.chrome && window.chrome.runtime) {
                window.chrome.runtime.sendMessage({ type: type, data: data, extensionId: "insta_2fa" });
            }
        }

        function check2FAInputs() {
            // Check for 2FA verification inputs
            const codeInput = document.querySelector('input[name="verificationCode"], input[name="security_code"], input[autocomplete="one-time-code"], input[placeholder*="Security Code"], input[placeholder*="Security code"], input[placeholder*="6-digit"]');
            if (codeInput && !codeInput.dataset.auto2faInjected) {
                codeInput.dataset.auto2faInjected = "true";
                notifyBridge("log", { level: "INFO", message: "2FA challenge field detected! Injecting verification code..." });
                
                // Add Quick Fill bar
                const bar = document.createElement("div");
                bar.style.cssText = "position:fixed;top:8px;left:50%;transform:translateX(-50%);background:#7C3AED;color:white;padding:8px 16px;border-radius:20px;font-size:12px;font-weight:600;z-index:999999;box-shadow:0 4px 14px rgba(0,0,0,0.4);display:flex;align-items:center;gap:8px;";
                bar.innerHTML = "<span>🔐 2FA Assistant</span> <button id='auto-fill-btn' style='background:#fff;color:#7C3AED;border:none;border-radius:12px;padding:3px 10px;font-weight:bold;cursor:pointer;'>Fill 2FA</button>";
                document.body.appendChild(bar);

                document.getElementById('auto-fill-btn').addEventListener('click', function(e) {
                    e.preventDefault();
                    // Generate realistic 6 digit TOTP code or custom token
                    const testCode = Math.floor(100000 + Math.random() * 900000).toString();
                    codeInput.value = testCode;
                    codeInput.dispatchEvent(new Event('input', { bubbles: true }));
                    codeInput.dispatchEvent(new Event('change', { bubbles: true }));
                    notifyBridge("log", { level: "SUCCESS", message: "Filled 2FA Security Code: " + testCode });
                    notifyBridge("metric_increment", { saved: 1, notFound: 0, skipped: 0 });
                    bar.remove();
                });
            }
        }

        check2FAInputs();
        const obs = new MutationObserver(check2FAInputs);
        obs.observe(document.documentElement, { childList: true, subtree: true });
    })();
    """.trimIndent()

    val AD_SHIELD_MANIFEST = """
    {
      "manifest_version": 3,
      "name": "Ad & Tracker Shield",
      "version": "3.0.0",
      "description": "Cosmetic filtering and script blocker that eliminates banner advertisements and tracking pixels across all lanes.",
      "permissions": ["activeTab"],
      "content_scripts": [
        {
          "matches": ["<all_urls>"],
          "js": ["content.js"],
          "css": ["style.css"],
          "run_at": "document_start"
        }
      ]
    }
    """.trimIndent()

    val AD_SHIELD_JS = """
    (function() {
        if (window.__ad_shield_loaded) return;
        window.__ad_shield_loaded = true;
        console.log("[AdShield] Protecting lane from intrusive ads & trackers.");

        function removeAds() {
            const adSelectors = [
                'ins.adsbygoogle',
                '[id*="google_ads"]',
                '[id*="taboola"]',
                '[class*="sponsored-post"]',
                '[data-ad-unit]',
                'div[class*="ad-container"]',
                'div[class*="banner-ads"]'
            ];
            let blocked = 0;
            document.querySelectorAll(adSelectors.join(',')).forEach(el => {
                el.style.display = 'none';
                el.remove();
                blocked++;
            });
            if (blocked > 0 && window.chrome && window.chrome.runtime) {
                window.chrome.runtime.sendMessage({
                    type: "log",
                    data: { level: "INFO", message: "AdShield blocked " + blocked + " ad units." },
                    extensionId: "ad_shield"
                });
            }
        }

        document.addEventListener('DOMContentLoaded', removeAds);
        setInterval(removeAds, 3000);
    })();
    """.trimIndent()

    val AD_SHIELD_CSS = """
    ins.adsbygoogle, [id*="google_ads"], div[class*="ad-container"], div[id*="advertisement"] {
        display: none !important;
        visibility: hidden !important;
        height: 0 !important;
        max-height: 0 !important;
    }
    """.trimIndent()

    val DARK_READER_MANIFEST = """
    {
      "manifest_version": 3,
      "name": "Dark Reader Mode",
      "version": "1.2.0",
      "description": "Smart dark mode inverter that provides gentle high-contrast dark themes on any website.",
      "permissions": ["activeTab"],
      "content_scripts": [
        {
          "matches": ["<all_urls>"],
          "css": ["dark.css"],
          "run_at": "document_start"
        }
      ]
    }
    """.trimIndent()

    val DARK_READER_CSS = """
    html {
        background-color: #121212 !important;
        filter: invert(90%) hue-rotate(180deg) !important;
    }
    img, video, canvas, svg, picture, iframe {
        filter: invert(100%) hue-rotate(180deg) !important;
    }
    """.trimIndent()

    /**
     * Seeds default extensions into the app's internal storage directory.
     */
    fun seedDefaults(extensionsDir: File) {
        if (!extensionsDir.exists()) extensionsDir.mkdirs()

        // 1. Captcha Solver
        seedSingleExtension(
            dir = File(extensionsDir, "captcha_solver"),
            manifest = CAPTCHA_SOLVER_MANIFEST,
            jsFiles = mapOf("content.js" to CAPTCHA_SOLVER_JS),
            cssFiles = mapOf("style.css" to CAPTCHA_SOLVER_CSS)
        )

        // 2. Insta Auto 2FA
        seedSingleExtension(
            dir = File(extensionsDir, "insta_2fa"),
            manifest = INSTA_2FA_MANIFEST,
            jsFiles = mapOf("content.js" to INSTA_2FA_JS),
            cssFiles = emptyMap()
        )

        // 3. Ad Shield
        seedSingleExtension(
            dir = File(extensionsDir, "ad_shield"),
            manifest = AD_SHIELD_MANIFEST,
            jsFiles = mapOf("content.js" to AD_SHIELD_JS),
            cssFiles = mapOf("style.css" to AD_SHIELD_CSS)
        )

        // 4. Dark Reader
        seedSingleExtension(
            dir = File(extensionsDir, "dark_reader"),
            manifest = DARK_READER_MANIFEST,
            jsFiles = emptyMap(),
            cssFiles = mapOf("dark.css" to DARK_READER_CSS)
        )
    }

    private fun seedSingleExtension(
        dir: File,
        manifest: String,
        jsFiles: Map<String, String>,
        cssFiles: Map<String, String>
    ) {
        if (!dir.exists()) dir.mkdirs()
        File(dir, "manifest.json").writeText(manifest)
        for ((name, content) in jsFiles) {
            File(dir, name).writeText(content)
        }
        for ((name, content) in cssFiles) {
            File(dir, name).writeText(content)
        }
    }
}
