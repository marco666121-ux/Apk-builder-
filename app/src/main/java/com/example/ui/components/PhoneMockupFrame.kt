package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ApkProject
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PhoneMockupFrame(
    project: ApkProject,
    modifier: Modifier = Modifier
) {
    var previewMode by remember { mutableStateOf("WEBVIEW") } // "WEBVIEW" or "SPLASH"
    var isSplashPlaying by remember { mutableStateOf(false) }
    var isPageLoading by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val statusColor = try {
        Color(android.graphics.Color.parseColor(project.statusBarColor))
    } catch (e: Exception) {
        Color(0xFF0A0F1D)
    }

    val navColor = try {
        Color(android.graphics.Color.parseColor(project.navBarColor))
    } catch (e: Exception) {
        Color(0xFF0A0F1D)
    }

    val splashBg = try {
        Color(android.graphics.Color.parseColor(project.splashBgColor))
    } catch (e: Exception) {
        Color(0xFF0A0F1D)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector Bar
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (previewMode == "WEBVIEW") MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.clickable { previewMode = "WEBVIEW" }
            ) {
                Text(
                    text = "Web App View",
                    color = if (previewMode == "WEBVIEW") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (previewMode == "SPLASH") MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.clickable {
                    previewMode = "SPLASH"
                    isSplashPlaying = true
                }
            ) {
                Text(
                    text = "Splash Screen",
                    color = if (previewMode == "SPLASH") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Phone Frame Body
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color(0xFF030712))
                .border(6.dp, Color(0xFF1E293B), RoundedCornerShape(36.dp))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(34.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Notch & Status Bar (respecting project status bar color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(if (project.isFullScreen) Color.Transparent else statusColor)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Time & Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "9:41",
                            fontSize = 11.sp,
                            color = if (project.isDarkSystemUi) Color.Black else Color.White
                        )
                        // Camera punch hole
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = if (project.isDarkSystemUi) Color.Black else Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (project.isDarkSystemUi) Color.Black else Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // App Screen Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(android.graphics.Color.parseColor(try { project.appBgColor } catch (e: Exception) { "#111827" })))
                ) {
                    if (previewMode == "SPLASH" || isSplashPlaying) {
                        // Splash Screen Preview
                        SplashPreviewView(
                            project = project,
                            bgColor = splashBg,
                            isPlaying = isSplashPlaying,
                            onFinish = {
                                isSplashPlaying = false
                                previewMode = "WEBVIEW"
                            }
                        )
                    } else {
                        // Live WebView Preview
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        webViewInstance = this
                                        settings.javaScriptEnabled = project.jsEnabled
                                        settings.domStorageEnabled = project.domStorageEnabled
                                        settings.setSupportZoom(project.zoomEnabled)
                                        settings.builtInZoomControls = project.zoomEnabled
                                        settings.displayZoomControls = false
                                        settings.cacheMode = WebSettings.LOAD_DEFAULT

                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isPageLoading = true
                                            }
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isPageLoading = false
                                            }
                                        }
                                        loadUrl(project.websiteUrl)
                                    }
                                },
                                update = { wv ->
                                    wv.settings.javaScriptEnabled = project.jsEnabled
                                    wv.settings.domStorageEnabled = project.domStorageEnabled
                                    wv.settings.setSupportZoom(project.zoomEnabled)
                                    if (wv.url != project.websiteUrl) {
                                        wv.loadUrl(project.websiteUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isPageLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // Phone Navigation Bar (respecting project nav bar color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(if (project.isFullScreen) Color.Transparent else navColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Android Home indicator pill
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
            }
        }

        // Quick Controls under Phone
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webViewInstance?.reload() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload Preview",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${project.appName} (v${project.versionName})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SplashPreviewView(
    project: ApkProject,
    bgColor: Color,
    isPlaying: Boolean,
    onFinish: () -> Unit
) {
    var currentDuration by remember { mutableStateOf(project.splashDurationSeconds) }

    LaunchedEffect(isPlaying, project.splashDurationSeconds) {
        if (isPlaying) {
            val durationMs = (project.splashDurationSeconds * 1000).toLong().coerceAtLeast(800)
            delay(durationMs)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppIconBadge(
                iconPreset = project.iconPreset,
                iconUri = project.iconUri,
                size = 72.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = project.appName,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Powered by Native WebView",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
