package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.ProjectRepository
import com.example.ui.screens.BuildHistoryScreen
import com.example.ui.screens.BuildProgressScreen
import com.example.ui.screens.BuildSuccessScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProjectEditorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebViewSandboxScreen
import com.example.ui.theme.ApkBuilderTheme
import com.example.ui.viewmodel.ApkBuilderViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ApkBuilderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = ProjectRepository(database.projectDao(), database.buildHistoryDao())
        viewModel = ApkBuilderViewModel(repository)

        setContent {
            ApkBuilderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ApkBuilderApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ApkBuilderApp(viewModel: ApkBuilderViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        Screen.EDITOR -> ProjectEditorScreen(viewModel = viewModel)
        Screen.BUILD_PROGRESS -> BuildProgressScreen(viewModel = viewModel)
        Screen.BUILD_SUCCESS -> BuildSuccessScreen(viewModel = viewModel)
        Screen.WEBVIEW_SANDBOX -> WebViewSandboxScreen(viewModel = viewModel)
        Screen.BUILD_HISTORY -> BuildHistoryScreen(viewModel = viewModel)
        Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
    }
}
