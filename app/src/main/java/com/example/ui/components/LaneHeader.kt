package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.LaneState
import com.example.ui.theme.AmberWarning
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
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSubtle

@Composable
fun LaneHeader(
    laneState: LaneState,
    onUrlChange: (String) -> Unit,
    onUrlSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onHome: () -> Unit,
    onToggleDesktop: () -> Unit,
    onOpenExtensions: () -> Unit,
    onOpenHistory: () -> Unit,
    onClearData: () -> Unit,
    onStartLane: () -> Unit,
    onStopLane: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showMenu by remember { mutableStateOf(false) }

    val laneAccentColor = when (laneState.laneId) {
        1 -> LaneColor1
        2 -> LaneColor2
        3 -> LaneColor3
        else -> LaneColor4
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateDark)
    ) {
        // --- 1. Status Bar (Exactly as requested: Lane X · Saved 0 · Not found 0 · Skipped 19) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateCard)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Lane Indicator Pill
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(laneAccentColor)
                )

                Text(
                    text = "Lane ${laneState.laneId} · Saved ${laneState.savedCount} · Not found ${laneState.notFoundCount} · Skipped ${laneState.skippedCount}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = TextLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status chip (Running, Paused, Idle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val (statusBg, statusText) = when (laneState.status) {
                    "RUNNING" -> EmeraldSuccess to "RUNNING"
                    "PAUSED" -> AmberWarning to "PAUSED"
                    "ERROR" -> RoseError to "ERROR"
                    else -> SlateMuted to "IDLE"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusBg.copy(alpha = 0.2f))
                        .border(0.5.dp, statusBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusBg
                    )
                }

                // Quick single-lane Start/Stop
                IconButton(
                    onClick = {
                        if (laneState.status == "RUNNING") onStopLane() else onStartLane()
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("lane_${laneState.laneId}_quick_toggle")
                ) {
                    Icon(
                        imageVector = if (laneState.status == "RUNNING") Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Toggle task",
                        tint = if (laneState.status == "RUNNING") RoseError else EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- 2. Compact URL Bar & Navigation Controls ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = laneState.canGoBack,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (laneState.canGoBack) TextLight else SlateMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = laneState.canGoForward,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_forward")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (laneState.canGoForward) TextLight else SlateMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Reload Button
            IconButton(
                onClick = onReload,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_reload")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    tint = TextLight,
                    modifier = Modifier.size(15.dp)
                )
            }

            // URL Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateCard)
                    .border(0.5.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    BasicTextField(
                        value = laneState.inputUrl,
                        onValueChange = onUrlChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lane_${laneState.laneId}_url_input"),
                        textStyle = TextStyle(
                            color = TextLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(laneAccentColor),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onUrlSubmit(laneState.inputUrl)
                            }
                        )
                    )

                    if (laneState.inputUrl.isNotBlank()) {
                        IconButton(
                            onClick = { onUrlChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = SlateMuted,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Desktop Mode Toggle
            IconButton(
                onClick = onToggleDesktop,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_desktop_toggle")
            ) {
                Icon(
                    imageVector = if (laneState.isDesktopMode) Icons.Default.Computer else Icons.Default.Smartphone,
                    contentDescription = "Desktop Mode",
                    tint = if (laneState.isDesktopMode) laneAccentColor else SlateMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Home Button
            IconButton(
                onClick = onHome,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_home")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = TextLight,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Extension Badge & Quick Toggle
            IconButton(
                onClick = onOpenExtensions,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("lane_${laneState.laneId}_extensions")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Extensions",
                        tint = if (laneState.activeExtensions.isNotEmpty()) laneAccentColor else SlateMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    if (laneState.activeExtensions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(laneAccentColor)
                        )
                    }
                }
            }

            // More Menu Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = SlateMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SlateCard)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = TextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Lane History", color = TextLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onOpenHistory()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = RoseError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Clear Lane Cache/Data", color = RoseError, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onClearData()
                        }
                    )
                }
            }
        }

        // Progress Bar
        AnimatedVisibility(visible = laneState.isLoading) {
            LinearProgressIndicator(
                progress = { laneState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = laneAccentColor,
                trackColor = SlateDark
            )
        }
    }
}
