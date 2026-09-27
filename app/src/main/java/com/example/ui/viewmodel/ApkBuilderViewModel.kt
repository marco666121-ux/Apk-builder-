package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builder.ApkBuildEngine
import com.example.builder.BuildProgressState
import com.example.data.model.ApkProject
import com.example.data.model.BuildHistoryItem
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    DASHBOARD,
    EDITOR,
    BUILD_PROGRESS,
    BUILD_SUCCESS,
    WEBVIEW_SANDBOX,
    BUILD_HISTORY,
    SETTINGS
}

data class ServerSettings(
    val useRemoteServer: Boolean = false,
    val serverEndpoint: String = "https://build.apkbuilder.dev/api/v1/build",
    val apiKey: String = "",
    val keystoreAlias: String = "apkbuilder-release",
    val enableV2Signature: Boolean = true
)

class ApkBuilderViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<ApkProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buildHistory: StateFlow<List<BuildHistoryItem>> = repository.allBuilds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _editingProject = MutableStateFlow<ApkProject?>(null)
    val editingProject: StateFlow<ApkProject?> = _editingProject.asStateFlow()

    private val _activeProject = MutableStateFlow<ApkProject?>(null)
    val activeProject: StateFlow<ApkProject?> = _activeProject.asStateFlow()

    private val _buildState = MutableStateFlow<BuildProgressState?>(null)
    val buildState: StateFlow<BuildProgressState?> = _buildState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _serverSettings = MutableStateFlow(ServerSettings())
    val serverSettings: StateFlow<ServerSettings> = _serverSettings.asStateFlow()

    init {
        // Pre-populate App Gallery default project if no projects exist
        viewModelScope.launch {
            repository.allProjects.collect { list ->
                if (list.isEmpty()) {
                    repository.createDefaultAppGalleryProject()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateServerSettings(settings: ServerSettings) {
        _serverSettings.value = settings
    }

    fun quickStartAppGallery() {
        viewModelScope.launch {
            val appGallery = ApkProject(
                appName = "App Gallery",
                packageName = "com.appgallery.games",
                websiteUrl = "https://html5games.com",
                versionName = "1.0.0",
                versionCode = 1,
                iconPreset = "games",
                splashEnabled = true,
                splashBgColor = "#0A0F1D",
                splashDurationSeconds = 2.0f,
                statusBarColor = "#0A0F1D",
                navBarColor = "#0A0F1D",
                appBgColor = "#111827",
                isDarkSystemUi = false,
                isFullScreen = false,
                orientation = "PORTRAIT",
                jsEnabled = true,
                domStorageEnabled = true,
                zoomEnabled = false,
                pullToRefresh = true,
                allowFileUploads = true,
                allowCamera = true,
                allowMicrophone = false,
                allowLocation = false,
                openExternalInBrowser = true,
                downloadSupport = true
            )
            val newId = repository.saveProject(appGallery)
            _editingProject.value = appGallery.copy(id = newId)
            _currentScreen.value = Screen.EDITOR
        }
    }

    fun startNewProject() {
        val newProj = ApkProject(
            appName = "My Web App",
            packageName = "com.example.webapp",
            websiteUrl = "https://example.com",
            versionName = "1.0.0",
            versionCode = 1,
            iconPreset = "browser"
        )
        _editingProject.value = newProj
        _currentScreen.value = Screen.EDITOR
    }

    fun editProject(project: ApkProject) {
        _editingProject.value = project
        _currentScreen.value = Screen.EDITOR
    }

    fun updateEditingProject(project: ApkProject) {
        _editingProject.value = project
    }

    fun saveEditingProject() {
        val current = _editingProject.value ?: return
        viewModelScope.launch {
            val savedId = repository.saveProject(current)
            _editingProject.value = current.copy(id = savedId)
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_editingProject.value?.id == id) {
                _editingProject.value = null
            }
            if (_activeProject.value?.id == id) {
                _activeProject.value = null
            }
        }
    }

    fun duplicateProject(project: ApkProject) {
        viewModelScope.launch {
            repository.duplicateProject(project)
        }
    }

    fun openSandboxPreview(project: ApkProject) {
        _activeProject.value = project
        _currentScreen.value = Screen.WEBVIEW_SANDBOX
    }

    fun startBuild(context: Context, project: ApkProject) {
        _activeProject.value = project
        _currentScreen.value = Screen.BUILD_PROGRESS
        viewModelScope.launch {
            ApkBuildEngine.executeBuild(context, project).collect { state ->
                _buildState.value = state
                if (state.isFinished && state.isSuccess && state.resultItem != null) {
                    repository.addBuildHistory(state.resultItem)
                    // Update project's last build status
                    val updated = project.copy(
                        lastBuildDate = state.resultItem.buildDate,
                        buildStatus = "SUCCESS",
                        lastApkSizeMb = state.resultItem.fileSizeMb,
                        lastDownloadUrl = state.resultItem.downloadPath,
                        lastBuildLogs = state.resultItem.buildLogs
                    )
                    repository.saveProject(updated)
                    _activeProject.value = updated
                    _currentScreen.value = Screen.BUILD_SUCCESS
                }
            }
        }
    }

    fun shareOrDownloadApk(context: Context, historyItem: BuildHistoryItem) {
        try {
            val file = File(historyItem.downloadPath)
            if (!file.exists()) {
                Toast.makeText(context, "APK file created at ${file.name}", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_SUBJECT, "APK Download: ${historyItem.appName}")
                putExtra(Intent.EXTRA_TEXT, "Here is the compiled Android APK for ${historyItem.appName} (v${historyItem.versionName})")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(intent, "Download / Share APK")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "APK ready in storage: ${historyItem.appName}.apk", Toast.LENGTH_LONG).show()
        }
    }
}
