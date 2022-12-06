package com.adafruit.pyleap.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.adafruit.pyleap.PyLeapApplication

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as PyLeapApplication).container
        setContent {
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
                PyLeapApp(appContainer, widthSizeClass)
        }
    }
}


