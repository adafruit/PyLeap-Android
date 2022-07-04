package com.adafruit.pyleap.ui.startup

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.openroad.ble.filetransfer.FileTransferConnectionManager
import kotlinx.coroutines.flow.*

class StartupViewModel(
    private val fileTransferConnectionManager: FileTransferConnectionManager,
    private val onFinished: () -> Unit,
) : ViewModel() {

    // UI State
    sealed class UiState {
        object AwaitingPermissionsCheck : UiState()
        object Reconnecting : UiState()
        object Finished : UiState()
    }


    // Internal state
    private data class ViewModelState(
        val state: UiState,
    ) {
        fun toUiState(): UiState = state
    }

    // Data - Private
    private val viewModelState = MutableStateFlow(ViewModelState(UiState.AwaitingPermissionsCheck))

    // Data
    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )


    fun permissionsChecked() {
        viewModelState.update { it.copy(state = UiState.Reconnecting) }

        // TODO (add reconnection)
        simulateFinishReconnection()
    }


    private fun simulateFinishReconnection() {
        viewModelState.update { it.copy(state = UiState.Finished) }
        onFinished()
    }


    /**
     * Factory that takes StartupViewModel as a dependency
     */
    companion object {
        fun provideFactory(
            fileTransferConnectionManager: FileTransferConnectionManager,
            onFinished: () -> Unit,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StartupViewModel(fileTransferConnectionManager, onFinished) as T
            }
        }
    }
}