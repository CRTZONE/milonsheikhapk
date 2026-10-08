package com.example.extension

import org.json.JSONArray
import org.json.JSONObject

data class ExtensionManifest(
    val manifestVersion: Int,
    val name: String,
    val version: String,
    val description: String,
    val contentScripts: List<ContentScript>,
    val permissions: List<String>,
    val backgroundScripts: List<String>
) {
    companion object {
        fun fromJson(jsonStr: String): ExtensionManifest {
            val json = JSONObject(jsonStr)
            val manifestVersion = json.optInt("manifest_version", 2)
            val name = json.optString("name", "Unnamed Extension")
            val version = json.optString("version", "1.0.0")
            val description = json.optString("description", "")

            // Permissions
            val permissionsList = mutableListOf<String>()
            val permArray = json.optJSONArray("permissions")
            if (permArray != null) {
                for (i in 0 until permArray.length()) {
                    permissionsList.add(permArray.optString(i))
                }
            }

            // Content Scripts
            val contentScriptsList = mutableListOf<ContentScript>()
            val csArray = json.optJSONArray("content_scripts")
            if (csArray != null) {
                for (i in 0 until csArray.length()) {
                    val csObj = csArray.optJSONObject(i) ?: continue
                    val matches = jsonArrayToStringList(csObj.optJSONArray("matches"))
                    val js = jsonArrayToStringList(csObj.optJSONArray("js"))
                    val css = jsonArrayToStringList(csObj.optJSONArray("css"))
                    val runAt = csObj.optString("run_at", "document_idle")

                    contentScriptsList.add(
                        ContentScript(
                            matches = matches,
                            jsFiles = js,
                            cssFiles = css,
                            runAt = runAt
                        )
                    )
                }
            }

            // Background scripts
            val bgScripts = mutableListOf<String>()
            if (manifestVersion == 3) {
                val bgObj = json.optJSONObject("background")
                val serviceWorker = bgObj?.optString("service_worker")
                if (!serviceWorker.isNullOrBlank()) {
                    bgScripts.add(serviceWorker)
                }
            } else {
                val bgObj = json.optJSONObject("background")
                val scriptsArr = bgObj?.optJSONArray("scripts")
                if (scriptsArr != null) {
                    bgScripts.addAll(jsonArrayToStringList(scriptsArr))
                }
            }

            return ExtensionManifest(
                manifestVersion = manifestVersion,
                name = name,
                version = version,
                description = description,
                contentScripts = contentScriptsList,
                permissions = permissionsList,
                backgroundScripts = bgScripts
            )
        }

        private fun jsonArrayToStringList(array: JSONArray?): List<String> {
            if (array == null) return emptyList()
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.optString(i)
                if (item.isNotBlank()) {
                    list.add(item)
                }
            }
            return list
        }
    }
}

data class ContentScript(
    val matches: List<String>,
    val jsFiles: List<String>,
    val cssFiles: List<String>,
    val runAt: String
)
