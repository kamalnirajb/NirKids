package com.nirkids.app.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nirkids.app.ui.main.nav.NavGraph
import com.nirkids.app.ui.theme.NirKidsTheme
import com.nirkids.app.ui.utils.TtsHelper
import com.nirkids.app.ui.utils.VibrationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val ttsHelper by lazy { TtsHelper(applicationContext) }
    private val vibrationHelper by lazy { VibrationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NirKidsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        ttsHelper = ttsHelper,
                        vibrationHelper = vibrationHelper
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.stop()
    }
}
