package com.example.browser

data class LaneState(
    val laneId: Int, // 1, 2, 3, 4
    val name: String = "Lane $laneId",
    val url: String = "https://www.google.com",
    val inputUrl: String = "https://www.google.com",
    val title: String = "New Tab",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val savedCount: Int = 0,
    val notFoundCount: Int = 0,
    val skippedCount: Int = 19,
    val status: String = "IDLE", // IDLE, RUNNING, PAUSED, ERROR
    val activeExtensions: List<String> = emptyList(),
    val errorMessage: String? = null
)
