package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

class LanePreferences(private val context: Context) {

    private fun getLanePrefs(laneId: Int): SharedPreferences {
        return context.getSharedPreferences("lane_prefs_$laneId", Context.MODE_PRIVATE)
    }

    fun getHomeUrl(laneId: Int): String {
        return getLanePrefs(laneId).getString("home_url", "https://www.google.com") ?: "https://www.google.com"
    }

    fun setHomeUrl(laneId: Int, url: String) {
        getLanePrefs(laneId).edit().putString("home_url", url).apply()
    }

    fun isDesktopMode(laneId: Int): Boolean {
        return getLanePrefs(laneId).getBoolean("desktop_mode", false)
    }

    fun setDesktopMode(laneId: Int, enabled: Boolean) {
        getLanePrefs(laneId).edit().putBoolean("desktop_mode", enabled).apply()
    }

    fun getZoomLevel(laneId: Int): Int {
        return getLanePrefs(laneId).getInt("zoom_level", 100)
    }

    fun setZoomLevel(laneId: Int, zoom: Int) {
        getLanePrefs(laneId).edit().putInt("zoom_level", zoom).apply()
    }

    /**
     * Provides an isolated sub-directory for each lane's assets, scripts, and local cache.
     */
    fun getLaneStorageDir(laneId: Int): File {
        val dir = File(context.filesDir, "lane_storage_$laneId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Returns a Desktop User-Agent string.
     */
    fun getDesktopUserAgent(): String {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    }

    /**
     * Returns a Mobile User-Agent string.
     */
    fun getMobileUserAgent(defaultUserAgent: String): String {
        return defaultUserAgent
    }

    /**
     * Clear all files in the isolated lane directory.
     */
    fun clearLaneStorage(laneId: Int) {
        val dir = getLaneStorageDir(laneId)
        dir.deleteRecursively()
        dir.mkdirs()
    }
}
