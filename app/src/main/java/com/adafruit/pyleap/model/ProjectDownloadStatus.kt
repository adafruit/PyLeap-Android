package com.adafruit.pyleap.model

/**
 * Created by Antonio García (antonio@openroad.es)
 */

sealed class ProjectDownloadStatus {
    object NotDownloaded : ProjectDownloadStatus()
    object Connecting:  ProjectDownloadStatus()
    data class Downloading(val progress: Float) : ProjectDownloadStatus()
    object Processing : ProjectDownloadStatus()
    object Downloaded : ProjectDownloadStatus()
    data class Error(val cause: Throwable) : ProjectDownloadStatus()

}