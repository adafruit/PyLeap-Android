package com.adafruit.pyleap.ui.startup

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.R
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake

@Composable
fun StartupScreen(
    startupViewModel: StartupViewModel,
) {
    /*
    // Check Bluetooth-related permissions state
    val bluetoothPermissionState =
        rememberMultiplePermissionsState(BleManager.getNeededPermissions())

    val isInitialPermissionsCheckInProgress =
        !bluetoothPermissionState.allPermissionsGranted && !bluetoothPermissionState.shouldShowRationale
    LaunchedEffect(isInitialPermissionsCheckInProgress) {
        if (isInitialPermissionsCheckInProgress) {
            // First time that permissions are needed at startup
            bluetoothPermissionState.launchMultiplePermissionRequest()
        } else {
            startupViewModel.permissionsChecked()
        }
    }*/

    // This screen is not used to do any initialization, so we go straight to onFinished
    val currentOnFinished by rememberUpdatedState(startupViewModel.onFinished)        // Don't recompose: https://developer.android.com/jetpack/compose/side-effects
    LaunchedEffect(true) {
        currentOnFinished()
    }

    // Splash Screen
    SplashScreen()
}

@Composable
private fun SplashScreen() {
    // val uiState by startupViewModel.uiState.collectAsState()

    // Splash screen
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )
        {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = "PyLeap logo",
                )

                /*
                Text(
                    "Restoring Connection...",
                    Modifier.alpha(if (uiState == StartupViewModel.UiState.Reconnecting) 1.0f else 0.0f)
                )*/

                CircularProgressIndicator()
            }
        }
    }
}


// region Previews
@Preview(showSystemUi = true)
@Composable
private fun StartupSmartPhonePreview() {
    PyLeapTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun StartupTabletPreview() {

    PyLeapTheme {
        SplashScreen()
    }
}
//endregion
