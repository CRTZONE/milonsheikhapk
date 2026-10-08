package com.example.extension

import android.webkit.JavascriptInterface
import org.json.JSONObject

interface ExtensionBridgeListener {
    fun onMetricUpdate(laneId: Int, savedDelta: Int, notFoundDelta: Int, skippedDelta: Int)
    fun onExtensionLog(laneId: Int, level: String, message: String)
    fun onExtensionEvent(laneId: Int, extensionId: String, eventType: String, payload: String)
}

class ExtensionBridge(
    private val laneId: Int,
    private val listener: ExtensionBridgeListener
) {
    private val localStorageMap = mutableMapOf<String, String>()

    @JavascriptInterface
    fun log(level: String, message: String) {
        listener.onExtensionLog(laneId, level.uppercase(), message)
    }

    @JavascriptInterface
    fun reportMetrics(saved: Int, notFound: Int, skipped: Int) {
        listener.onMetricUpdate(laneId, saved, notFound, skipped)
    }

    @JavascriptInterface
    fun onExtensionMessage(extensionId: String, jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val type = json.optString("type", "generic")
            val payload = json.optString("data", jsonString)

            when (type) {
                "metric_increment" -> {
                    val s = json.optInt("saved", 0)
                    val nf = json.optInt("notFound", 0)
                    val sk = json.optInt("skipped", 0)
                    listener.onMetricUpdate(laneId, s, nf, sk)
                }
                "log" -> {
                    val lvl = json.optString("level", "INFO")
                    val msg = json.optString("message", "")
                    listener.onExtensionLog(laneId, lvl, msg)
                }
                else -> {
                    listener.onExtensionEvent(laneId, extensionId, type, payload)
                }
            }
        } catch (e: Exception) {
            listener.onExtensionLog(laneId, "WARN", "Bridge message parse error: ${e.message}")
        }
    }

    @JavascriptInterface
    fun getStorage(key: String): String {
        return localStorageMap[key] ?: ""
    }

    @JavascriptInterface
    fun setStorage(key: String, value: String) {
        localStorageMap[key] = value
    }
}
