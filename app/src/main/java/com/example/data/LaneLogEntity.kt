package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lane_logs",
    indices = [
        Index(value = ["laneId"]),
        Index(value = ["timestamp"])
    ]
)
data class LaneLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val laneId: Int, // 0 for System, 1..4 for individual lanes
    val level: String, // "INFO", "SUCCESS", "WARN", "ERROR"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
