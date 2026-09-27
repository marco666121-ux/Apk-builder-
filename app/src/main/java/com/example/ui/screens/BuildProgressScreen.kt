package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.builder.BuildProgressState
import com.example.builder.StepStatus
import com.example.data.model.ApkProject
import com.example.ui.components.AppIconBadge
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TechBlue
import com.example.ui.viewmodel.ApkBuilderViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun BuildProgressScreen(
    viewModel: ApkBuilderViewModel,
    modifier: Modifier = Modifier
) {
    val buildState by viewModel.buildState.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val project = activeProject

    var showLogsDrawer by remember { mutableStateOf(false) }
    val logsListState = rememberLazyListState()

    val animatedProgress by animateFloatAsState(
        targetValue = buildState?.progressFraction ?: 0.05f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "buildProgress"
    )

    // Auto-scroll logs
    LaunchedEffect(buildState?.consoleLogs?.size) {
        val count = buildState?.consoleLogs?.size ?: 0
        if (count > 0) {
            logsListState.animateScrollToItem(count - 1)
        }
    }

    BackHandler(enabled = buildState?.isFinished != false) {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (project != null) {
                        AppIconBadge(
                            iconPreset = project.iconPreset,
                            iconUri = project.iconUri,
                            size = 44.dp
                        )
                    }
                    Column {
                        Text(
                            text = if (buildState?.isSuccess == true) "APK Build Complete" else "Building APK...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project?.appName ?: "Project"} (${project?.packageName ?: ""})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (buildState?.isFinished == true) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Progress Bar & Percentage
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (buildState?.isSuccess == true) "100% Ready"
                               else "Step ${(buildState?.currentStepIndex ?: 0) + 1} of ${buildState?.totalSteps ?: 7}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (buildState?.errorMessage != null) RoseError else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Steps Checklist Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val steps = buildState?.steps ?: emptyList()
                    items(steps, key = { it.id }) { step ->
                        BuildStepRow(step = step)
                    }

                    // Error Box if failed
                    if (buildState?.errorMessage != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.1f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoseError)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseError)
                                        Text("Build Failed", fontWeight = FontWeight.Bold, color = RoseError)
                                    }
                                    Text(
                                        text = buildState?.errorMessage ?: "An unexpected error occurred during APK build.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (buildState?.technicalDetails != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = buildState?.technicalDetails ?: "",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = Color(0xFFFCA5A5),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Console Logs Drawer Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogsDrawer = !showLogsDrawer },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Build Console Output (${buildState?.consoleLogs?.size ?: 0} lines)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (showLogsDrawer) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    AnimatedVisibility(visible = showLogsDrawer) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF030712))
                                .padding(8.dp)
                        ) {
                            LazyColumn(
                                state = logsListState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val logs = buildState?.consoleLogs ?: emptyList()
                                items(logs) { line ->
                                    val isErr = line.contains("ERROR") || line.contains("FAILED")
                                    val isSuccess = line.contains("SUCCESS") || line.contains("✓")
                                    Text(
                                        text = line,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (isErr) Color(0xFFF87171)
                                               else if (isSuccess) Color(0xFF34D399)
                                               else Color(0xFF94A3B8),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons (if build finished)
            if (buildState?.isFinished == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (buildState?.isSuccess == true) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.BUILD_SUCCESS) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_view_build_success")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Generated APK")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.EDITOR) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back to Editor")
                        }
                        Button(
                            onClick = {
                                if (project != null) {
                                    // Retry build
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry Build")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildStepRow(step: com.example.builder.BuildStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status indicator icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (step.status) {
                        StepStatus.COMPLETED -> EmeraldSuccess.copy(alpha = 0.15f)
                        StepStatus.RUNNING -> TechBlue.copy(alpha = 0.15f)
                        StepStatus.FAILED -> RoseError.copy(alpha = 0.15f)
                        StepStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .border(
                    width = 1.dp,
                    color = when (step.status) {
                        StepStatus.COMPLETED -> EmeraldSuccess
                        StepStatus.RUNNING -> TechBlue
                        StepStatus.FAILED -> RoseError
                        StepStatus.PENDING -> Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (step.status) {
                StepStatus.COMPLETED -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(18.dp)
                )
                StepStatus.RUNNING -> CircularProgressIndicator(
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(18.dp),
                    color = TechBlue
                )
                StepStatus.FAILED -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Failed",
                    tint = RoseError,
                    modifier = Modifier.size(18.dp)
                )
                StepStatus.PENDING -> Text(
                    text = "${step.id + 1}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (step.status == StepStatus.RUNNING) FontWeight.Bold else FontWeight.SemiBold,
                color = if (step.status == StepStatus.COMPLETED) EmeraldSuccess
                       else if (step.status == StepStatus.FAILED) RoseError
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = step.logOutput ?: step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
