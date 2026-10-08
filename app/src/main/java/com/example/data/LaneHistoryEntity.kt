package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lane_history",
    foreignKeys = [
        ForeignKey(
            entity = LaneEntity::class,
            parentColumns = ["id"],
            childColumns = ["laneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["laneId"]),
        Index(value = ["timestamp"])
    ]
)
data class LaneHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val laneId: Int,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)
