package com.adafruit.pyleap.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.openroad.filetransfer.Peripheral
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.filetransfer.Scanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConnectionCardViewModel(
    connectionManager: ConnectionManager,
) : ViewModel() {
    // UI State
    sealed class UiState {
        data class Connected(val peripheral: Peripheral) : UiState()
        data class Scanning(val peripherals: List<Peripheral>) : UiState()
        data class Error(val message: String) : UiState()

    }

    // Data - Private
//    private val bleErrorStatus
//    private val wifiErrorStatus:

    // Internal state
    private data class ViewModelState(
        val connectionManager: ConnectionManager,
    ) {
        fun toUiState(): UiState {
            var result: UiState = UiState.Error("Scanning Undefined State")
            val connectedPeripheral = connectionManager.currentFileTransferClient.value?.peripheral

            connectedPeripheral?.let {
                result = UiState.Connected(it)
            } ?: run {
                if (connectionManager.isScanning) {
                    result = when (val state = connectionManager.scanningState.value) {
                        is Scanner.ScanningState.Idle -> UiState.Scanning(emptyList())
                        is Scanner.ScanningState.Scanning -> UiState.Scanning(state.peripherals)
                        is Scanner.ScanningState.ScanningError -> UiState.Error(
                            state.cause.message ?: "Scanning Error"
                        )
                        else -> UiState.Error("Scanning Undefined State")
                    }
                } else {
                    result = UiState.Error("Scanning not Started")
                }
            }

            return result
        }
    }

    // Data - Private
    private val viewModelState = MutableStateFlow(ViewModelState(connectionManager))

    // Data
    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )
}