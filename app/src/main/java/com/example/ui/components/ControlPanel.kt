package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSubtle

@Composable
fun ControlPanel(
    screenCount: Int,
    onSelectScreens: (Int) -> Unit,
    onStartAll: () -> Unit,
    onPauseAll: () -> Unit,
    onStopAll: () -> Unit,
    onOpenExtensions: () -> Unit,
    onOpenBatchUrl: () -> Unit,
    onOpenLogs: () -> Unit,
    recentLogCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface)
            .border(width = 1.dp, color = SlateCardBorder)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // --- Row 1: Start / Pause / Stop + Quick Action Launcher ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Primary Control Buttons (Start / Pause / Stop)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START
                Button(
                    onClick = onStartAll,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("control_btn_start"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = Color.White
                    ),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Start",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // PAUSE
                Button(
                    onClick = onPauseAll,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("control_btn_pause"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberWarning,
                        contentColor = Color.Black
                    ),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pause",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // STOP
                Button(
                    onClick = onStopAll,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("control_btn_stop"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseError,
                        contentColor = Color.White
                    ),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Stop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Quick Launcher Icons (Extensions, Batch URL, Logs)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Extensions Launcher
                IconButton(
                    onClick = onOpenExtensions,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                        .testTag("control_btn_extensions")
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Extensions",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Batch URL
                IconButton(
                    onClick = onOpenBatchUrl,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                        .testTag("control_btn_batch_url")
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = "Batch URL",
                        tint = TextLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Logs Sheet Launcher
                IconButton(
                    onClick = onOpenLogs,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                        .testTag("control_btn_logs")
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Execution Logs",
                        tint = if (recentLogCount > 0) EmeraldSuccess else SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Row 2: Screens Segmented Selector (1, 2, 3, 4) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Screens:",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Segmented Pills [1] [2] [3] [4]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .border(0.5.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (count in 1..4) {
                        val isSelected = (screenCount == count)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyanPrimary else Color.Transparent)
                                .clickable { onSelectScreens(count) }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                                .testTag("screens_select_$count"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) SlateDark else TextLight
                            )
                        }
                    }
                }
            }

            // Quick status info text
            Text(
                text = "Multi-Instance Engine",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SlateMuted
            )
        }
    }
}
