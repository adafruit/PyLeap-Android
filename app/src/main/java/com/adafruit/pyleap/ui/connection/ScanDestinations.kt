package com.adafruit.pyleap.ui.connection

/**
 * Created by Antonio García (antonio@openroad.es)
 */

sealed class ScanDestinations(val route: String) {
    object Scan : ScanDestinations("scan")
}
