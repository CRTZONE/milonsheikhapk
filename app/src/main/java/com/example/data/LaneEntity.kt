package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lanes")
data class LaneEntity(
    @PrimaryKey val id: Int, // 1, 2, 3, 4
    val name: String,
    val currentUrl: String = "https://www.google.com",
    val isDesktopMode: Boolean = false,
    val savedCount: Int = 0,
    val notFoundCount: Int = 0,
    val skippedCount: Int = 0,
    val status: String = "IDLE", // IDLE, RUNNING, PAUSED, ERROR
    val enabledExtensions: String = "" // comma-separated extension ids
)
