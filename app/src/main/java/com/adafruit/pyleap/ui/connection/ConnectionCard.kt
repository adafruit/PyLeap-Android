package com.adafruit.pyleap.ui.connection

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.ui.theme.ConnectionStatusError
import com.adafruit.pyleap.ui.theme.ConnectionStatusScanning
import com.adafruit.pyleap.ui.theme.ConnectionStatusSuccess
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import io.openroad.filetransfer.ble.peripheral.BondedBlePeripherals
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.peripheral.WifiPeripheral

@Composable
fun ConnectionCard(
    connectionManager: ConnectionManager,
    bondedBlePeripherals: BondedBlePeripherals?,
    connectionCardViewModel: ConnectionCardViewModel = ConnectionCardViewModel(connectionManager = connectionManager, bondedBlePeripherals = bondedBlePeripherals),
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

    Button(
        onClick = onOpenScanDialog,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
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

                is ConnectionCardViewModel.UiState.Scanning -> {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        /* Disabled: Too distracting
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp, top = 4.dp),//.border(1.dp, Color.Red),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )*/

                        val numPeripherals = uiState.numPeripherals
                        Text(
                            "Scanning... ($numPeripherals ${if (numPeripherals == 1) "peripheral" else "peripherals"} found)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                is ConnectionCardViewModel.UiState.Connected -> {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            buildAnnotatedString {
                                append(
                                    AnnotatedString("Connected to: ", spanStyle = SpanStyle(color = Color.White))
                                )

                                append(
                                    AnnotatedString(
                                        uiState.peripheral.nameOrAddress, spanStyle = SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = Color.White
                                    ))
                                )
                            }
                        )
                    }
                }
            }

            val actionText: String? = when (uiState) {
                is ConnectionCardViewModel.UiState.Scanning -> "Select"
                is ConnectionCardViewModel.UiState.Connected -> "Change"
                else -> null
            }

            actionText?.let {
                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    onClick = onOpenScanDialog,
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 30.dp)
                ) {
                    Text(it.uppercase())
                }
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
        ConnectionCardContents(ConnectionCardViewModel.UiState.Error("Show error message")) {}
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
        ConnectionCardContents(ConnectionCardViewModel.UiState.Scanning(listOf(wifiPeripheral).size)) {}
    }
}

@Preview()
@Composable
fun ConnectionCard_Connected_Preview() {
    val wifiPeripheral = WifiPeripheral("Adafruit Test 3384743 Blah blah blah Long name", "http://192.168.0.1", 80)

    PyLeapTheme {
        ConnectionCardContents(ConnectionCardViewModel.UiState.Connected(wifiPeripheral)) {}
    }
}


// endregion