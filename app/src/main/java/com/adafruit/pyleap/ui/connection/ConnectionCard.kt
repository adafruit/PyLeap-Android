package com.adafruit.pyleap.ui.connection

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.ui.theme.ConnectionStatusError
import com.adafruit.pyleap.ui.theme.ConnectionStatusScanning
import com.adafruit.pyleap.ui.theme.ConnectionStatusSuccess
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.peripheral.WifiPeripheral

@Composable
fun ConnectionCard(
    connectionManager: ConnectionManager,
    connectionCardViewModel: ConnectionCardViewModel = ConnectionCardViewModel(connectionManager = connectionManager),
    onOpenScanDialog: () -> Unit,
) {
    val uiState by connectionCardViewModel.uiState.collectAsState()

    ConnectionCardContents(uiState = uiState, onOpenScanDialog = onOpenScanDialog)
}

@Composable
private fun ConnectionCardContents(
    uiState: ConnectionCardViewModel.UiState,
    onOpenScanDialog: () -> Unit,
) {
    val backgroundColor = when (uiState) {
        is ConnectionCardViewModel.UiState.Connected -> ConnectionStatusSuccess
        is ConnectionCardViewModel.UiState.Scanning -> ConnectionStatusScanning
        is ConnectionCardViewModel.UiState.Error -> ConnectionStatusError
    }

    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        when (uiState) {
            is ConnectionCardViewModel.UiState.Error -> {
                Text(
                    uiState.message,
                    //getTextForBluetoothState(uiState.bleState),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            /*
            ConnectionViewModel.UiState.NotConnected -> {
                Text(
                    "Not connected to a device.",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                TextButton(onClick = onOpenScanDialog) {
                    Text(
                        "Connect now",
                        style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
                        color = Color.White
                    )
                }
            }*/

            is ConnectionCardViewModel.UiState.Scanning -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp, top = 4.dp),//.border(1.dp, Color.Red),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Text(
                    "Scanning... ${uiState.peripherals.size} peripherals found",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            is ConnectionCardViewModel.UiState.Connected -> {
                Text(
                    "Connected to ",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )

                Text(
                    uiState.peripheral.nameOrAddress,
                    style = MaterialTheme.typography.labelLarge.copy(fontStyle = FontStyle.Italic),
                    color = Color.White
                )
            }
        }
    }
}

/*
private fun getTextForBluetoothState(bleState: BleState): String {
    return when (bleState) {
        BleState.Unknown -> "Unknown Bluetooth status"
        BleState.BluetoothNotAvailable -> "Bluetooth not available"
        BleState.BleNotAvailable -> "Bluetooth Low Energy not available"
        BleState.Disabled -> "Bluetooth Disabled"
        BleState.Enabled -> "Bluetooth Enabled"
        BleState.TurningOn -> "Bluetooth Turning On"
        BleState.TurningOff -> "Bluetooth Turning Off"
    }
}*/

// region Preview
@Preview()
@Composable
fun ConnectionCard_BleStatus_Preview() {
    PyLeapTheme {
        ConnectionCardContents(ConnectionCardViewModel.UiState.Error("Bluetooth not available")) {}
    }
}
/*
@Preview()
@Composable
fun ConnectionCard_NotConnected_Preview() {
    PyLeapTheme {
        ConnectionCardContents(ConnectionViewModel.UiState.NotConnected) {}
    }
}*/


@Preview()
@Composable
fun ConnectionCard_Scanning_Preview() {
    val wifiPeripheral = WifiPeripheral("Adafruit Test", "http://192.168.0.1", 80)
    PyLeapTheme {
        ConnectionCardContents(ConnectionCardViewModel.UiState.Scanning(listOf(wifiPeripheral))) {}
    }
}

@Preview()
@Composable
fun ConnectionCard_Connected_Preview() {
    val wifiPeripheral = WifiPeripheral("Adafruit Test", "http://192.168.0.1", 80)

    PyLeapTheme {
        ConnectionCardContents(ConnectionCardViewModel.UiState.Connected(wifiPeripheral)) {}
    }
}


// endregion