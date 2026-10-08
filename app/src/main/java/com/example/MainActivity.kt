package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LaneViewModel
import com.example.ui.MultiBrowserScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: LaneViewModel = viewModel()
                val screenLayout by viewModel.screenLayout.collectAsState()
                val focusedLaneId by viewModel.focusedLaneId.collectAsState()
                val laneStates by viewModel.laneStates.collectAsState()

                // Custom BackHandler: If in single screen mode and can go back, navigate back
                BackHandler {
                    val activeLane = if (screenLayout == 1) focusedLaneId else 1
                    val state = laneStates[activeLane]
                    if (state?.canGoBack == true) {
                        viewModel.goBack(activeLane)
                    } else if (screenLayout != 4) {
                        viewModel.setScreenLayout(4) // return to 2x2 grid
                    } else {
                        finish()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SlateDark
                ) {
                    MultiBrowserScreen(viewModel = viewModel)
                }
            }
        }
    }
}
