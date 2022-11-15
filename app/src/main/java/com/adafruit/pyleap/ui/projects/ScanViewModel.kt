package com.adafruit.pyleap.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.openroad.filetransfer.filetransfer.ConnectionManager

/**
 * Created by Antonio García (antonio@openroad.es)
 */

class ScanViewModel(
    private val connectionManager: ConnectionManager,
) : ViewModel() {

    // region Lifecycle
    fun onResume() {
        // Start scanning if we are in the scanning state
        startScan()
    }

    fun onPause() {
        stopScan()
    }
    // endregion

    // region Actions
    private fun startScan() {
        //stopScan()
        connectionManager.startScan()
    }

    private fun stopScan() {
        connectionManager.stopScan()
    }
    //


    companion object {
        fun provideFactory(
            connectionManager: ConnectionManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScanViewModel(
                    connectionManager,
                ) as T
            }
        }
    }
}