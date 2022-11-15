package com.adafruit.pyleap.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adafruit.glider.utils.LogUtils
import io.openroad.filetransfer.Peripheral
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.filetransfer.Scanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
        val connectionManagerState: Scanner.ScanningState,
        val connectedPeripheral: Peripheral?,
    ) {
        fun toUiState(): UiState {
            var result: UiState = UiState.Error("Scanning Undefined State")

            connectedPeripheral?.let {
                result = UiState.Connected(it)
            } ?: run {
                result = when (connectionManagerState) {
                    is Scanner.ScanningState.Idle -> UiState.Scanning(emptyList())
                    is Scanner.ScanningState.Scanning -> UiState.Scanning(connectionManagerState.peripherals)
                    is Scanner.ScanningState.ScanningError -> UiState.Error(
                        connectionManagerState.cause.message ?: "Scanning Error"
                    )
                    else -> UiState.Error("Scanning Undefined State")
                }
            }

            return result
        }
    }

    // Data - Private
    // private val log by LogUtils()

    private val viewModelState = MutableStateFlow(
        ViewModelState(
            connectionManagerState = connectionManager.scanningState.value,
            connectedPeripheral = connectionManager.currentFileTransferClient.value?.peripheral
        )
    )

    // Data
    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )


    init {
        // Monitor scanning
        viewModelScope.launch {
            connectionManager.scanningState.collect { state ->
                //log.info("Connection state: $state")

                // Update internal state
                viewModelState.update { it.copy(connectionManagerState = state) }
            }
        }

        // Monitor selected peripheral
        viewModelScope.launch {
            connectionManager.currentFileTransferClient.collect { fileTransferClient ->

                val connectedPeripheral = fileTransferClient?.peripheral
                //log.info("Connected to: ${connectedPeripheral?.nameOrAddress ?: "<nil>"}")

                // Update internal state
                viewModelState.update { it.copy(connectedPeripheral = connectedPeripheral) }
            }
        }
    }
}