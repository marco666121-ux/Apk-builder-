package com.example.generator

import com.example.data.model.ApkProject
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class GeneratedFile(
    val relativePath: String,
    val content: String
)

object AndroidProjectGenerator {

    fun generateProjectFiles(project: ApkProject): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        
        // 1. AndroidManifest.xml
        files.add(GeneratedFile("app/src/main/AndroidManifest.xml", generateManifest(project)))
        
        // 2. MainActivity.kt (Native WebView Activity)
        val packagePath = project.packageName.replace('.', '/')
        files.add(GeneratedFile("app/src/main/java/$packagePath/MainActivity.kt", generateMainActivity(project)))
        
        // 3. build.gradle.kts (App level)
        files.add(GeneratedFile("app/build.gradle.kts", generateAppBuildGradle(project)))
        
        // 4. settings.gradle.kts
        files.add(GeneratedFile("settings.gradle.kts", "rootProject.name = \"${project.appName}\"\ninclude(\":app\")\n"))
        
        // 5. strings.xml
        files.add(GeneratedFile("app/src/main/res/values/strings.xml", generateStringsXml(project)))
        
        // 6. colors.xml
        files.add(GeneratedFile("app/src/main/res/values/colors.xml", generateColorsXml(project)))
        
        // 7. themes.xml
        files.add(GeneratedFile("app/src/main/res/values/themes.xml", generateThemesXml(project)))

        // 8. build-config.json (Project manifest)
        files.add(GeneratedFile("apk-builder-config.json", generateBuildConfigJson(project)))

        return files
    }

    private fun generateManifest(project: ApkProject): String {
        val permissions = mutableListOf<String>()
        permissions.add("android.permission.INTERNET")
        permissions.add("android.permission.ACCESS_NETWORK_STATE")
        if (project.allowCamera) permissions.add("android.permission.CAMERA")
        if (project.allowMicrophone) permissions.add("android.permission.RECORD_AUDIO")
        if (project.allowLocation) {
            permissions.add("android.permission.ACCESS_FINE_LOCATION")
            permissions.add("android.permission.ACCESS_COARSE_LOCATION")
        }
        if (project.downloadSupport || project.allowFileUploads) {
            permissions.add("android.permission.READ_EXTERNAL_STORAGE")
        }

        val screenOrientation = when (project.orientation) {
            "LANDSCAPE" -> "android:screenOrientation=\"landscape\""
            "PORTRAIT" -> "android:screenOrientation=\"portrait\""
            else -> "android:screenOrientation=\"unspecified\""
        }

        val permTags = permissions.joinToString("\n    ") {
            "<uses-permission android:name=\"$it\" />"
        }

        return """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${project.packageName}">

    $permTags

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.WebViewApp">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            $screenOrientation
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/Theme.WebViewApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
        """.trimIndent()
    }

    private fun generateMainActivity(project: ApkProject): String {
        return """
package ${project.packageName}

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: FrameLayout
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Status and Navigation Bar Styling
        setupSystemBars()

        // 2. Build Root Layout Programmatically
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.parseColor("${project.appBgColor}"))

        // WebView
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Configure WebView Settings
        webView.settings.apply {
            javaScriptEnabled = ${project.jsEnabled}
            domStorageEnabled = ${project.domStorageEnabled}
            setSupportZoom(${project.zoomEnabled})
            builtInZoomControls = ${project.zoomEnabled}
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            databaseEnabled = true
            allowFileAccess = ${project.allowFileUploads}
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // Loading ProgressBar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = false
            max = 100
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                8
            )
        }

        // Error Layout
        errorLayout = createErrorLayout()
        errorLayout.visibility = View.GONE

        // Pull to refresh if enabled
        ${if (project.pullToRefresh) """
        swipeRefreshLayout = SwipeRefreshLayout(this).apply {
            addView(webView)
            setOnRefreshListener {
                if (isNetworkAvailable()) {
                    webView.reload()
                } else {
                    isRefreshing = false
                    showError()
                }
            }
        }
        rootLayout.addView(swipeRefreshLayout)
        """ else """
        rootLayout.addView(webView)
        """}

        rootLayout.addView(progressBar)
        rootLayout.addView(errorLayout)
        setContentView(rootLayout)

        // Setup Clients
        setupWebViewClients()

        // Handle Back Navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        // Initial Load
        loadWebsite()
    }

    private fun setupSystemBars() {
        ${if (project.isFullScreen) """
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        """ else """
        window.statusBarColor = Color.parseColor("${project.statusBarColor}")
        window.navigationBarColor = Color.parseColor("${project.navBarColor}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val appearance = if (${project.isDarkSystemUi}) {
                0
            } else {
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            }
            window.insetsController?.setSystemBarsAppearance(appearance, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        """}
    }

    private fun setupWebViewClients() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                errorLayout.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                swipeRefreshLayout?.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    showError()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                val mainHost = Uri.parse("${project.websiteUrl}").host ?: ""

                ${if (project.openExternalInBrowser) """
                if (request.url.host != null && !request.url.host.equals(mainHost, ignoreCase = true)) {
                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                    startActivity(intent)
                    return true
                }
                """ else """
                // Open all links inside WebView
                """}
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                ${if (project.allowLocation) "callback?.invoke(origin, true, false)" else "callback?.invoke(origin, false, false)"}
            }
        }
    }

    private fun loadWebsite() {
        if (isNetworkAvailable()) {
            webView.loadUrl("${project.websiteUrl}")
        } else {
            showError()
        }
    }

    private fun showError() {
        errorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun createErrorLayout(): FrameLayout {
        return FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("${project.appBgColor}"))
            val textView = TextView(this@MainActivity).apply {
                text = "Connection Offline\nPlease check your internet connection."
                setTextColor(Color.WHITE)
                textSize = 16f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }
            addView(textView)
            setOnClickListener {
                if (isNetworkAvailable()) {
                    errorLayout.visibility = View.GONE
                    webView.reload()
                }
            }
        }
    }
}
        """.trimIndent()
    }

    private fun generateAppBuildGradle(project: ApkProject): String {
        return """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "${project.packageName}"
    compileSdk = 34

    defaultConfig {
        applicationId = "${project.packageName}"
        minSdk = 24
        targetSdk = 34
        versionCode = ${project.versionCode}
        versionName = "${project.versionName}"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
        """.trimIndent()
    }

    private fun generateStringsXml(project: ApkProject): String {
        return """
<resources>
    <string name="app_name">${project.appName}</string>
</resources>
        """.trimIndent()
    }

    private fun generateColorsXml(project: ApkProject): String {
        return """
<resources>
    <color name="status_bar_color">${project.statusBarColor}</color>
    <color name="nav_bar_color">${project.navBarColor}</color>
    <color name="app_bg_color">${project.appBgColor}</color>
</resources>
        """.trimIndent()
    }

    private fun generateThemesXml(project: ApkProject): String {
        return """
<resources>
    <style name="Theme.WebViewApp" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="android:statusBarColor">${project.statusBarColor}</item>
        <item name="android:navigationBarColor">${project.navBarColor}</item>
        <item name="android:windowBackground">${project.appBgColor}</item>
    </style>
</resources>
        """.trimIndent()
    }

    private fun generateBuildConfigJson(project: ApkProject): String {
        return """
{
  "appName": "${project.appName}",
  "packageName": "${project.packageName}",
  "websiteUrl": "${project.websiteUrl}",
  "versionName": "${project.versionName}",
  "versionCode": ${project.versionCode},
  "settings": {
    "jsEnabled": ${project.jsEnabled},
    "domStorage": ${project.domStorageEnabled},
    "zoom": ${project.zoomEnabled},
    "pullToRefresh": ${project.pullToRefresh},
    "fileUploads": ${project.allowFileUploads},
    "camera": ${project.allowCamera},
    "microphone": ${project.allowMicrophone},
    "location": ${project.allowLocation},
    "externalInBrowser": ${project.openExternalInBrowser},
    "fullScreen": ${project.isFullScreen},
    "orientation": "${project.orientation}"
  },
  "builtWith": "APK Builder v2.0"
}
        """.trimIndent()
    }

    fun createProjectZipBytes(project: ApkProject): ByteArray {
        val files = generateProjectFiles(project)
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for (file in files) {
                val entry = ZipEntry("${project.appName.replace(" ", "_")}/${file.relativePath}")
                zos.putNextEntry(entry)
                zos.write(file.content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }
}
