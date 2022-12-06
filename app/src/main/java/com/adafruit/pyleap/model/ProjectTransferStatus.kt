package com.adafruit.pyleap.model

/**
 * Created by Antonio García (antonio@openroad.es)
 */

sealed class ProjectTransferStatus {
    object NotTransferred: ProjectTransferStatus()
    data class Transferring(val progress: Float) : ProjectTransferStatus()
    object Transferred : ProjectTransferStatus()
    data class Error(val cause: Throwable) : ProjectTransferStatus()
}