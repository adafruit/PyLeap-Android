package com.adafruit.pyleap.ui.connection

import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.openroad.ble.applicationContext
import io.openroad.ble.filetransfer.BleFileTransferPeripheral
import io.openroad.ble.filetransfer.FileTransferConnectionManager
import io.openroad.ble.scanner.BlePeripheralScanner
import io.openroad.ble.scanner.BlePeripheralScannerImpl
import io.openroad.ble.scanner.FakeBlePeripheralScannerImpl
import io.openroad.ble.state.BleState
import io.openroad.ble.state.BleStateRepository
import io.openroad.utils.LogUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectionViewModel(
    //context: Context,
    isInPreviewMode: Boolean,
    private val bleStateRepository: BleStateRepository,
    //private val blePeripheralScanner: BlePeripheralScanner,
    private val fileTransferConnectionManager: FileTransferConnectionManager,
) : ViewModel() {

    // UI State
    sealed class UiState {
        data class BleStateInfo(val bleState: BleState) : UiState()
        object NotConnected : UiState()
        data class Scanning(val scanUiState: ScanUiState) : UiState()
        data class Connected(val selectedPeripheral: String) : UiState()

        sealed class ScanUiState {
            object Scanning : ScanUiState()
            data class ScanningError(val cause: Throwable) : ScanUiState()
            object RestoringConnection : ScanUiState()
            object SetupConnection : ScanUiState()
            object Connecting : ScanUiState()
            object Connected : ScanUiState()
            object Discovering : ScanUiState()
            object SetupFileTransfer : ScanUiState()
            object Bonding : ScanUiState()
            data class FileTransferEnabled(val fileTransferPeripheral: BleFileTransferPeripheral) :
                ScanUiState()

            data class FileTransferError(val gattErrorCode: Int) : ScanUiState()
            data class Disconnected(val cause: Throwable?) : ScanUiState()
        }
    }

    // Internal state
    private data class ViewModelState(
        val bleState: BleState,
        val isScanning: Boolean,
        val peripherals: List<String>,       // TODO
    ) {
        fun toUiState(): UiState =
            if (bleState != BleState.Enabled) {
                UiState.BleStateInfo(bleState = bleState)
            } else {
                UiState.NotConnected
            }
    }

    private val viewModelState = MutableStateFlow(
        ViewModelState(
            bleState = BleState.Unknown,
            isScanning = false,
            peripherals = emptyList()
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

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val log by LogUtils()
    private var blePeripheralScanner: BlePeripheralScanner

    init {
        // Init peripheral scanner (or use a fake if in preview mode)
        blePeripheralScanner = if (isInPreviewMode) {
            FakeBlePeripheralScannerImpl(viewModelScope)        // Use Fake implementation
        } else {
            BlePeripheralScannerImpl(applicationContext, null, viewModelScope)
        }

        // Update internal status based on collected flows
        viewModelScope.launch {
            bleStateRepository.bleState.collect { bleState ->
                viewModelState.update { it.copy(bleState = bleState) }
            }
        }
    }

    /**
     * Factory that takes ConnectionViewModel as a dependency
     */
    companion object {
        fun provideFactory(
            isInPreviewMode: Boolean,
            bleStateRepository: BleStateRepository,
            //blePeripheralScanner: BlePeripheralScanner,
            fileTransferConnectionManager: FileTransferConnectionManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
               // val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                return ConnectionViewModel(
                    //application.applicationContext,
                    isInPreviewMode,
                    bleStateRepository,
                    //blePeripheralScanner,
                    fileTransferConnectionManager
                ) as T
            }
        }
    }
}