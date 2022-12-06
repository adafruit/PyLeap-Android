package com.adafruit.pyleap.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by Antonio García (antonio@openroad.es)
 */

class PyLeapProject(
    val data: ProjectData,
    downloadStatus: ProjectDownloadStatus,
) {
    private var _downloadState = MutableStateFlow(downloadStatus)
    val downloadState = _downloadState.asStateFlow()

    private var _transferState = MutableStateFlow<ProjectTransferStatus>(ProjectTransferStatus.NotTransferred)
    val transferState = _transferState.asStateFlow()

    fun setDownloadState(value: ProjectDownloadStatus) {
        _downloadState.update { value }
    }

    fun setTransferState(value: ProjectTransferStatus) {
        _transferState.update { value }
    }
}
