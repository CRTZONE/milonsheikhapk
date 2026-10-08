package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.browser.LaneManager
import com.example.browser.LaneState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSubtle

@Composable
fun LaneWebView(
    laneState: LaneState,
    laneManager: LaneManager,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("lane_${laneState.laneId}_webview_container")
    ) {
        AndroidView(
            factory = { context ->
                val webView = laneManager.getOrCreateWebView(laneState.laneId)
                // Detach from previous parent if needed
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView
            },
            update = { webView ->
                // WebView is retained; no need to reload on every recomposition
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error overlay if page failed to load
        if (laneState.errorMessage != null && !laneState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SlateDark.copy(alpha = 0.95f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = RoseError,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Failed to load webpage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = laneState.errorMessage,
                        fontSize = 11.sp,
                        color = TextSubtle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ElevatedButton(
                        onClick = { laneManager.reload(laneState.laneId) },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
