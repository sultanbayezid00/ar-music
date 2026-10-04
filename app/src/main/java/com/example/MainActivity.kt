package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MusicViewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.ARBackground
import com.example.ui.theme.ARMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ARBackground
                ) {
                    val viewModel: MusicViewModel = viewModel()
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
