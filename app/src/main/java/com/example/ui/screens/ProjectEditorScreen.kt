package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ApkProject
import com.example.ui.components.AppIconBadge
import com.example.ui.components.ColorPickerField
import com.example.ui.components.PhoneMockupFrame
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TechBlue
import com.example.ui.viewmodel.ApkBuilderViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    viewModel: ApkBuilderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projectState by viewModel.editingProject.collectAsStateWithLifecycle()
    val project = projectState ?: return

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLivePreviewModal by remember { mutableStateOf(false) }

    // Validation checks
    val isUrlValid = project.websiteUrl.startsWith("http://") || project.websiteUrl.startsWith("https://")
    val isPackageValid = project.packageName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+$"))
    val isNameValid = project.appName.isNotBlank() && project.appName.length <= 40

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateEditingProject(project.copy(iconUri = uri.toString()))
        }
    }

    val splashPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateEditingProject(project.copy(splashImageUri = uri.toString()))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (project.id == 0L) "New APK Project" else "Edit ${project.appName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = project.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Dashboard")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLivePreviewModal = !showLivePreviewModal },
                        modifier = Modifier.testTag("btn_toggle_live_preview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Toggle Preview",
                            tint = if (showLivePreviewModal) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.saveEditingProject() },
                        modifier = Modifier.testTag("btn_save_draft")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Draft")
                    }
                    Button(
                        onClick = {
                            viewModel.saveEditingProject()
                            viewModel.startBuild(context, project)
                        },
                        enabled = isUrlValid && isNameValid && isPackageValid,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_editor_build_apk")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Build APK")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth > 800.dp

            if (isWideScreen) {
                // Wide Screen: Side-by-side Editor & Live Phone Preview
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxSize()
                    ) {
                        EditorTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                        EditorContent(
                            selectedTab = selectedTab,
                            project = project,
                            isUrlValid = isUrlValid,
                            isPackageValid = isPackageValid,
                            isNameValid = isNameValid,
                            onUpdate = { viewModel.updateEditingProject(it) },
                            onPickIcon = { photoPickerLauncher.launch("image/*") },
                            onPickSplash = { splashPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Side Live Preview Pane
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PhoneMockupFrame(project = project)
                    }
                }
            } else {
                // Mobile Layout: Tabs + Scrollable form, with optional collapsible preview
                Column(modifier = Modifier.fillMaxSize()) {
                    EditorTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

                    if (showLivePreviewModal) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PhoneMockupFrame(project = project)
                        }
                    }

                    EditorContent(
                        selectedTab = selectedTab,
                        project = project,
                        isUrlValid = isUrlValid,
                        isPackageValid = isPackageValid,
                        isNameValid = isNameValid,
                        onUpdate = { viewModel.updateEditingProject(it) },
                        onPickIcon = { photoPickerLauncher.launch("image/*") },
                        onPickSplash = { splashPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun EditorTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("General", "App Icon & Splash", "Appearance", "WebView Settings")
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(title, fontSize = 12.sp, maxLines = 1) },
                modifier = Modifier.testTag("tab_editor_$index")
            )
        }
    }
}

@Composable
fun EditorContent(
    selectedTab: Int,
    project: ApkProject,
    isUrlValid: Boolean,
    isPackageValid: Boolean,
    isNameValid: Boolean,
    onUpdate: (ApkProject) -> Unit,
    onPickIcon: () -> Unit,
    onPickSplash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (selectedTab) {
            0 -> TabGeneral(
                project = project,
                isUrlValid = isUrlValid,
                isPackageValid = isPackageValid,
                isNameValid = isNameValid,
                onUpdate = onUpdate
            )
            1 -> TabIconAndSplash(
                project = project,
                onUpdate = onUpdate,
                onPickIcon = onPickIcon,
                onPickSplash = onPickSplash
            )
            2 -> TabAppearance(
                project = project,
                onUpdate = onUpdate
            )
            3 -> TabWebViewSettings(
                project = project,
                onUpdate = onUpdate
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun TabGeneral(
    project: ApkProject,
    isUrlValid: Boolean,
    isPackageValid: Boolean,
    isNameValid: Boolean,
    onUpdate: (ApkProject) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // App Name
            OutlinedTextField(
                value = project.appName,
                onValueChange = { onUpdate(project.copy(appName = it)) },
                label = { Text("App Name *") },
                supportingText = {
                    if (!isNameValid) Text("App name cannot be empty (max 40 chars)", color = MaterialTheme.colorScheme.error)
                    else Text("The name shown on the Android home screen")
                },
                isError = !isNameValid,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_app_name")
            )

            // Website URL
            OutlinedTextField(
                value = project.websiteUrl,
                onValueChange = { onUpdate(project.copy(websiteUrl = it.trim())) },
                label = { Text("Website URL *") },
                placeholder = { Text("https://your-website.com") },
                supportingText = {
                    if (!isUrlValid) Text("Must start with https:// or http://", color = MaterialTheme.colorScheme.error)
                    else Text("Your website will open inside native WebView")
                },
                isError = !isUrlValid,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_website_url")
            )

            // Quick website URL presets
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick URLs:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        onUpdate(project.copy(websiteUrl = "https://html5games.com", appName = "App Gallery"))
                    }
                ) {
                    Text("App Gallery Games", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        onUpdate(project.copy(websiteUrl = "https://wikipedia.org"))
                    }
                ) {
                    Text("Wikipedia", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            // Package Name
            OutlinedTextField(
                value = project.packageName,
                onValueChange = { onUpdate(project.copy(packageName = it.trim())) },
                label = { Text("Package Name *") },
                placeholder = { Text("com.company.app") },
                supportingText = {
                    if (!isPackageValid) Text("Must follow Android format: com.example.app", color = MaterialTheme.colorScheme.error)
                    else Text("Unique Android application ID")
                },
                isError = !isPackageValid,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_package_name")
            )

            // Version Name & Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = project.versionName,
                    onValueChange = { onUpdate(project.copy(versionName = it)) },
                    label = { Text("Version Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = project.versionCode.toString(),
                    onValueChange = {
                        val num = it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 1
                        onUpdate(project.copy(versionCode = num))
                    },
                    label = { Text("Version Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TabIconAndSplash(
    project: ApkProject,
    onUpdate: (ApkProject) -> Unit,
    onPickIcon: () -> Unit,
    onPickSplash: () -> Unit
) {
    // App Icon Section
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "App Icon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Upload an image or pick from designer presets. Android adaptive mipmaps (mdpi to xxxhdpi) are generated automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIconBadge(
                    iconPreset = project.iconPreset,
                    iconUri = project.iconUri,
                    size = 72.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPickIcon,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_upload_icon")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (project.iconUri == null) "Upload Icon" else "Replace")
                        }

                        if (project.iconUri != null) {
                            OutlinedButton(
                                onClick = { onUpdate(project.copy(iconUri = null)) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove")
                            }
                        }
                    }
                    Text(
                        text = if (project.iconUri != null) "Custom image loaded" else "Using preset: ${project.iconPreset}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Presets
            Text("Or select a Preset Icon:", style = MaterialTheme.typography.labelMedium)
            val presets = listOf(
                "games" to "Games",
                "browser" to "Web",
                "rocket" to "Launch",
                "shopping" to "Store",
                "code" to "Tech"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (presetKey, label) ->
                    val isSelected = project.iconPreset == presetKey && project.iconUri == null
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clickable { onUpdate(project.copy(iconPreset = presetKey, iconUri = null)) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            AppIconBadge(iconPreset = presetKey, size = 28.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // Splash Screen Section
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Native Splash Screen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Displays smooth loading brand transition while WebView initializes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = project.splashEnabled,
                    onCheckedChange = { onUpdate(project.copy(splashEnabled = it)) },
                    modifier = Modifier.testTag("switch_splash_screen")
                )
            }

            if (project.splashEnabled) {
                // Splash Duration Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Splash Duration", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format("%.1f", project.splashDurationSeconds)}s", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = project.splashDurationSeconds,
                        onValueChange = { onUpdate(project.copy(splashDurationSeconds = it)) },
                        valueRange = 0.5f..5.0f,
                        steps = 8
                    )
                }

                // Splash Background Color
                ColorPickerField(
                    label = "Splash Background Color",
                    selectedHex = project.splashBgColor,
                    onColorSelected = { onUpdate(project.copy(splashBgColor = it)) }
                )

                // Custom Splash Graphic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Splash Graphic", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = onPickSplash,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (project.splashImageUri == null) "Select Image" else "Replace Image")
                    }
                }
            }
        }
    }
}

@Composable
fun TabAppearance(
    project: ApkProject,
    onUpdate: (ApkProject) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "System UI & Layout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Screen Orientation
            Text("Screen Orientation", style = MaterialTheme.typography.labelMedium)
            val orientations = listOf(
                "PORTRAIT" to "Portrait",
                "LANDSCAPE" to "Landscape",
                "UNSPECIFIED" to "Auto-Rotate"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                orientations.forEach { (key, label) ->
                    val isSelected = project.orientation == key
                    Button(
                        onClick = { onUpdate(project.copy(orientation = key)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }

            // Fullscreen Mode Toggle
            SwitchSettingRow(
                title = "Full-Screen Mode (Immersive)",
                subtitle = "Hides Android status bar and navigation bar (ideal for games)",
                checked = project.isFullScreen,
                onCheckedChange = { onUpdate(project.copy(isFullScreen = it)) }
            )

            // Light/Dark System UI
            SwitchSettingRow(
                title = "Light System Bar Icons",
                subtitle = "Inverts status bar icons (clock, battery) for dark status bar backgrounds",
                checked = !project.isDarkSystemUi,
                onCheckedChange = { onUpdate(project.copy(isDarkSystemUi = !it)) }
            )

            // Status Bar Color
            ColorPickerField(
                label = "Status Bar Color",
                selectedHex = project.statusBarColor,
                onColorSelected = { onUpdate(project.copy(statusBarColor = it)) }
            )

            // Navigation Bar Color
            ColorPickerField(
                label = "Navigation Bar Color",
                selectedHex = project.navBarColor,
                onColorSelected = { onUpdate(project.copy(navBarColor = it)) }
            )

            // Webview Container Background Color
            ColorPickerField(
                label = "App Window Background",
                selectedHex = project.appBgColor,
                onColorSelected = { onUpdate(project.copy(appBgColor = it)) }
            )
        }
    }
}

@Composable
fun TabWebViewSettings(
    project: ApkProject,
    onUpdate: (ApkProject) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "WebView Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SwitchSettingRow(
                title = "JavaScript Enabled",
                subtitle = "Allows interactive JS scripts, games, and web apps to execute",
                checked = project.jsEnabled,
                onCheckedChange = { onUpdate(project.copy(jsEnabled = it)) }
            )

            SwitchSettingRow(
                title = "DOM Storage (Local Storage)",
                subtitle = "Enables localStorage and sessionStorage for logins and saved games",
                checked = project.domStorageEnabled,
                onCheckedChange = { onUpdate(project.copy(domStorageEnabled = it)) }
            )

            SwitchSettingRow(
                title = "Pinch to Zoom",
                subtitle = "Allows user to zoom in/out with multi-touch gestures",
                checked = project.zoomEnabled,
                onCheckedChange = { onUpdate(project.copy(zoomEnabled = it)) }
            )

            SwitchSettingRow(
                title = "Pull to Refresh",
                subtitle = "Allows swiping down from the top to reload the webpage",
                checked = project.pullToRefresh,
                onCheckedChange = { onUpdate(project.copy(pullToRefresh = it)) }
            )

            SwitchSettingRow(
                title = "Enable Download Support",
                subtitle = "Handles download triggers and prompts Android DownloadManager",
                checked = project.downloadSupport,
                onCheckedChange = { onUpdate(project.copy(downloadSupport = it)) }
            )

            SwitchSettingRow(
                title = "Open External Links in Browser",
                subtitle = "Links outside your main domain open in system Chrome/browser",
                checked = project.openExternalInBrowser,
                onCheckedChange = { onUpdate(project.copy(openExternalInBrowser = it)) }
            )
        }
    }

    // Android Hardware Permissions
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Android Permissions & Hardware",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SwitchSettingRow(
                title = "Allow File Uploads",
                subtitle = "Injects WebChromeClient file chooser for <input type='file'>",
                checked = project.allowFileUploads,
                onCheckedChange = { onUpdate(project.copy(allowFileUploads = it)) }
            )

            SwitchSettingRow(
                title = "Camera Permission",
                subtitle = "Allows website to access device camera for scanners or webcams",
                checked = project.allowCamera,
                onCheckedChange = { onUpdate(project.copy(allowCamera = it)) }
            )

            SwitchSettingRow(
                title = "Microphone Permission",
                subtitle = "Allows audio recording and voice calls inside WebView",
                checked = project.allowMicrophone,
                onCheckedChange = { onUpdate(project.copy(allowMicrophone = it)) }
            )

            SwitchSettingRow(
                title = "Location Permission",
                subtitle = "Supports HTML5 Geolocation API (navigator.geolocation)",
                checked = project.allowLocation,
                onCheckedChange = { onUpdate(project.copy(allowLocation = it)) }
            )
        }
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
