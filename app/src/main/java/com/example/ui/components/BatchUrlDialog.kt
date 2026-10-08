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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSubtle

@Composable
fun BatchUrlDialog(
    currentUrls: List<String>,
    onApplyBatchUrls: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Same URL for all, 1: Individual URLs
    var singleUrl by remember { mutableStateOf("https://www.google.com") }

    var url1 by remember { mutableStateOf(currentUrls.getOrNull(0) ?: "https://www.google.com") }
    var url2 by remember { mutableStateOf(currentUrls.getOrNull(1) ?: "https://www.instagram.com") }
    var url3 by remember { mutableStateOf(currentUrls.getOrNull(2) ?: "https://www.reddit.com") }
    var url4 by remember { mutableStateOf(currentUrls.getOrNull(3) ?: "https://news.ycombinator.com") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Batch Navigate Lanes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: All Same vs Custom per Lane
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateDark,
                    contentColor = CyanPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("All Lanes Same URL", fontSize = 12.sp, color = if (selectedTab == 0) CyanPrimary else SlateMuted) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Per-Lane Custom URL", fontSize = 12.sp, color = if (selectedTab == 1) CyanPrimary else SlateMuted) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Google" to "https://www.google.com",
                        "Instagram" to "https://www.instagram.com",
                        "Wikipedia" to "https://www.wikipedia.org",
                        "Reddit" to "https://www.reddit.com"
                    ).forEach { (label, targetUrl) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateCard)
                                .border(0.5.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    singleUrl = targetUrl
                                    url1 = targetUrl
                                    url2 = targetUrl
                                    url3 = targetUrl
                                    url4 = targetUrl
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = singleUrl,
                        onValueChange = { singleUrl = it },
                        label = { Text("URL for all 4 lanes", color = SlateMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_single_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = url1,
                            onValueChange = { url1 = it },
                            label = { Text("Lane 1 URL", color = SlateMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = url2,
                            onValueChange = { url2 = it },
                            label = { Text("Lane 2 URL", color = SlateMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = url3,
                            onValueChange = { url3 = it },
                            label = { Text("Lane 3 URL", color = SlateMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = url4,
                            onValueChange = { url4 = it },
                            label = { Text("Lane 4 URL", color = SlateMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            onApplyBatchUrls(listOf(singleUrl, singleUrl, singleUrl, singleUrl))
                        } else {
                            onApplyBatchUrls(listOf(url1, url2, url3, url4))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("apply_batch_urls_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = SlateDark
                    )
                ) {
                    Text(
                        text = "Load Across Lanes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
