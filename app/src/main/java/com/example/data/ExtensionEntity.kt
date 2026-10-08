package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey val id: String, // e.g. "captcha_solver", "insta_2fa", "ad_shield"
    val name: String,
    val version: String,
    val description: String,
    val enabledGlobal: Boolean = true,
    val isBuiltIn: Boolean = false,
    val manifestJson: String = "",
    val extractedDirPath: String = "",
    val iconBase64: String = "",
    val installedTimestamp: Long = System.currentTimeMillis()
)
