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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.R
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.ble.utils.BleManager
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartupScreen(
    startupViewModel: StartupViewModel,
) {
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
    }

    // Splash Screen
    SplashScreen(startupViewModel = startupViewModel)
}

@Composable
private fun SplashScreen(
    startupViewModel: StartupViewModel,
) {
    val uiState by startupViewModel.uiState.collectAsState()

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

                Text(
                    "Restoring Connection...",
                    Modifier.alpha(if (uiState == StartupViewModel.UiState.Reconnecting) 1.0f else 0.0f)
                )

                CircularProgressIndicator()
            }
        }
    }
}


// region Previews
@Preview(showSystemUi = true)
@Composable
private fun StartupSmartPhonePreview() {

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    val startupViewModel: StartupViewModel = viewModel(
        factory = StartupViewModel.provideFactory(
            connectionManager = connectionManager,
            onFinished = {},
        )
    )

    PyLeapTheme {
        SplashScreen(startupViewModel = startupViewModel)
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun StartupTabletPreview() {
    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    val startupViewModel: StartupViewModel = viewModel(
        factory = StartupViewModel.provideFactory(
            connectionManager = connectionManager,
            onFinished = {},
        )
    )

    PyLeapTheme {
        SplashScreen(startupViewModel = startupViewModel)
    }
}
//endregion
