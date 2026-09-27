package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "build_history")
data class BuildHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val buildDate: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "FAILED"
    val fileSizeMb: Double = 4.6,
    val sha256Fingerprint: String = "",
    val buildLogs: String = "",
    val downloadPath: String = ""
)
