package com.example.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.LaneState
import com.example.ui.components.BatchUrlDialog
import com.example.ui.components.ControlPanel
import com.example.ui.components.ExecutionLogSheet
import com.example.ui.components.ExtensionDialog
import com.example.ui.components.HistoryDialog
import com.example.ui.components.LaneHeader
import com.example.ui.components.LaneWebView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LaneColor1
import com.example.ui.theme.LaneColor2
import com.example.ui.theme.LaneColor3
import com.example.ui.theme.LaneColor4
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.delay

@Composable
fun MultiBrowserScreen(
    viewModel: LaneViewModel,
    modifier: Modifier = Modifier
) {
    val laneStates by viewModel.laneStates.collectAsState()
    val extensions by viewModel.allExtensions.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val screenLayout by viewModel.screenLayout.collectAsState()
    val focusedLaneId by viewModel.focusedLaneId.collectAsState()

    val showExtensionsDialog by viewModel.showExtensionsDialog.collectAsState()
    val showBatchUrlDialog by viewModel.showBatchUrlDialog.collectAsState()
    val showLogsSheet by viewModel.showLogsSheet.collectAsState()
    val historyLaneId by viewModel.historyLaneId.collectAsState()
    val selectedLogLane by viewModel.selectedLogLane.collectAsState()
    val notification by viewModel.notification.collectAsState()

    // Auto-dismiss notification after 3 seconds
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3000)
            viewModel.dismissNotification()
        }
    }

    // Total metrics across lanes
    val totalSaved = laneStates.values.sumOf { it.savedCount }
    val totalRunning = laneStates.values.count { it.status == "RUNNING" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        // --- 1. Top App Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = SlateDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MultiBrowser",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "LEMUR ENGINE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }
                    Text(
                        text = "4-Lane Isolated WebView Instance Grid",
                        fontSize = 10.sp,
                        color = TextSubtle
                    )
                }
            }

            // Top Quick Metrics & Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Total Saved badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateCard)
                        .border(0.5.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Total Saved: $totalSaved",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }

                // Batch URL launcher
                IconButton(
                    onClick = { viewModel.openBatchUrlDialog() },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = "Batch URL",
                        tint = TextLight,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Extensions launcher
                IconButton(
                    onClick = { viewModel.openExtensionsDialog() },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Extensions",
                        tint = CyanPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Logs launcher
                IconButton(
                    onClick = { viewModel.openLogsSheet() },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Execution Logs",
                        tint = if (logs.isNotEmpty()) EmeraldSuccess else SlateMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // --- 2. Main Lanes Display Viewport ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SlateDark)
        ) {
            when (screenLayout) {
                // 1 Screen: Focused Fullscreen with lane selector pill bar
                1 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Lane Selector Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateCard)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Focus Lane:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSubtle
                            )
                            for (id in 1..4) {
                                val isSelected = (focusedLaneId == id)
                                val laneColor = when (id) {
                                    1 -> LaneColor1
                                    2 -> LaneColor2
                                    3 -> LaneColor3
                                    else -> LaneColor4
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) laneColor else SlateDark)
                                        .clickable { viewModel.setFocusedLane(id) }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Lane $id",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SlateDark else TextLight
                                    )
                                }
                            }
                        }

                        val lane = laneStates[focusedLaneId] ?: LaneState(focusedLaneId)
                        LaneCard(
                            laneState = lane,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 2 Screens: 2-split view (Lane 1 & Lane 2)
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LaneCard(
                            laneState = laneStates[1] ?: LaneState(1),
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        LaneCard(
                            laneState = laneStates[2] ?: LaneState(2),
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }

                // 3 Screens: 1 large top + 2 split bottom
                3 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LaneCard(
                            laneState = laneStates[1] ?: LaneState(1),
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            LaneCard(
                                laneState = laneStates[2] ?: LaneState(2),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                            LaneCard(
                                laneState = laneStates[3] ?: LaneState(3),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }

                // 4 Screens: REAL 2x2 Grid (Default)
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Top Row: Lane 1 and Lane 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            LaneCard(
                                laneState = laneStates[1] ?: LaneState(1),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                            LaneCard(
                                laneState = laneStates[2] ?: LaneState(2),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }

                        // Bottom Row: Lane 3 and Lane 4
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            LaneCard(
                                laneState = laneStates[3] ?: LaneState(3),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                            LaneCard(
                                laneState = laneStates[4] ?: LaneState(4),
                                viewModel = viewModel,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Notification Overlay Snackbar
            val currentNotif = notification
            if (currentNotif != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Snackbar(
                        containerColor = if (currentNotif.isError) RoseError else SlateCard,
                        contentColor = TextLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(currentNotif.message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 3. Bottom Control Panel ---
        ControlPanel(
            screenCount = screenLayout,
            onSelectScreens = { viewModel.setScreenLayout(it) },
            onStartAll = { viewModel.startAll() },
            onPauseAll = { viewModel.pauseAll() },
            onStopAll = { viewModel.stopAll() },
            onOpenExtensions = { viewModel.openExtensionsDialog() },
            onOpenBatchUrl = { viewModel.openBatchUrlDialog() },
            onOpenLogs = { viewModel.openLogsSheet() },
            recentLogCount = logs.size
        )
    }

    // --- Dialogs & Sheets ---
    if (showExtensionsDialog) {
        ExtensionDialog(
            extensions = extensions,
            lanes = laneStates,
            onToggleExtensionForLane = { laneId, extId ->
                viewModel.toggleLaneExtension(laneId, extId)
            },
            onUploadZip = { uri ->
                viewModel.uploadExtensionZip(uri)
            },
            onDismiss = { viewModel.closeExtensionsDialog() }
        )
    }

    if (showBatchUrlDialog) {
        BatchUrlDialog(
            currentUrls = listOf(
                laneStates[1]?.url ?: "",
                laneStates[2]?.url ?: "",
                laneStates[3]?.url ?: "",
                laneStates[4]?.url ?: ""
            ),
            onApplyBatchUrls = { urls ->
                viewModel.batchNavigate(urls)
                viewModel.closeBatchUrlDialog()
            },
            onDismiss = { viewModel.closeBatchUrlDialog() }
        )
    }

    if (showLogsSheet) {
        ExecutionLogSheet(
            logs = logs,
            selectedLaneFilter = selectedLogLane,
            onSelectLaneFilter = { viewModel.setSelectedLogLane(it) },
            onClearLogs = { viewModel.clearLogs() },
            onDismiss = { viewModel.closeLogsSheet() }
        )
    }

    historyLaneId?.let { laneId ->
        val historyList by viewModel.getHistoryForLane(laneId).collectAsState()
        HistoryDialog(
            laneId = laneId,
            historyList = historyList,
            onSelectUrl = { url ->
                viewModel.navigateLane(laneId, url)
                viewModel.closeHistoryDialog()
            },
            onClearHistory = {
                viewModel.clearHistoryForLane(laneId)
            },
            onDismiss = { viewModel.closeHistoryDialog() }
        )
    }
}

@Composable
private fun LaneCard(
    laneState: LaneState,
    viewModel: LaneViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(0.5.dp, SlateCardBorder)
            .testTag("lane_card_${laneState.laneId}"),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SlateDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LaneHeader(
                laneState = laneState,
                onUrlChange = { viewModel.updateInputUrl(laneState.laneId, it) },
                onUrlSubmit = { viewModel.navigateLane(laneState.laneId, it) },
                onBack = { viewModel.goBack(laneState.laneId) },
                onForward = { viewModel.goForward(laneState.laneId) },
                onReload = { viewModel.reload(laneState.laneId) },
                onHome = { viewModel.goHome(laneState.laneId) },
                onToggleDesktop = { viewModel.toggleDesktopMode(laneState.laneId) },
                onOpenExtensions = { viewModel.openExtensionsDialog() },
                onOpenHistory = { viewModel.openHistoryDialog(laneState.laneId) },
                onClearData = { viewModel.clearLaneData(laneState.laneId) },
                onStartLane = { viewModel.startLane(laneState.laneId) },
                onStopLane = { viewModel.stopLane(laneState.laneId) }
            )

            LaneWebView(
                laneState = laneState,
                laneManager = viewModel.laneManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
