package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apk_projects")
data class ApkProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String = "App Gallery",
    val packageName: String = "com.appgallery.games",
    val websiteUrl: String = "https://html5games.com",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    
    // Icon
    val iconUri: String? = null,
    val iconPreset: String = "games", // "games", "browser", "rocket", "shopping", "code", "star"
    
    // Splash screen
    val splashEnabled: Boolean = true,
    val splashImageUri: String? = null,
    val splashBgColor: String = "#0A0F1D",
    val splashDurationSeconds: Float = 2.0f,
    
    // Appearance
    val statusBarColor: String = "#0A0F1D",
    val navBarColor: String = "#0A0F1D",
    val appBgColor: String = "#111827",
    val isDarkSystemUi: Boolean = false,
    val isFullScreen: Boolean = false,
    val orientation: String = "PORTRAIT", // "PORTRAIT", "LANDSCAPE", "UNSPECIFIED"
    
    // WebView Settings
    val jsEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val zoomEnabled: Boolean = false,
    val pullToRefresh: Boolean = true,
    val allowFileUploads: Boolean = true,
    val allowCamera: Boolean = true,
    val allowMicrophone: Boolean = false,
    val allowLocation: Boolean = false,
    val openExternalInBrowser: Boolean = true,
    val downloadSupport: Boolean = true,
    
    // Metadata & Build Status
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis(),
    val lastBuildDate: Long? = null,
    val buildStatus: String = "DRAFT", // "DRAFT", "BUILDING", "SUCCESS", "FAILED"
    val lastApkSizeMb: Double? = null,
    val lastDownloadUrl: String? = null,
    val lastBuildLogs: String? = null
)
