package com.adafruit.pyleap.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

// From: https://stackoverflow.com/questions/71591775/jetpack-compose-getting-light-vs-dark-mode-from-materialtheme-using-material-3

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5