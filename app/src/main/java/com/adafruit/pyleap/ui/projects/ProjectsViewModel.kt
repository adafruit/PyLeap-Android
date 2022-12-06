package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adafruit.glider.utils.LogUtils
import com.adafruit.pyleap.model.ProjectData
import com.adafruit.pyleap.model.ProjectDownloadStatus
import com.adafruit.pyleap.model.ProjectTransferStatus
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.repository.ProjectsRepository
import io.openroad.filetransfer.filetransfer.FileTransferClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.utf8Size
import java.io.File
import java.util.*

class ProjectsViewModel(
    private val autoSelectFirstProjectAfterLoading: Boolean,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {
    // UI State
    sealed class UiState {
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Projects(
            val projects: List<PyLeapProject>,
            val selectedProject: PyLeapProject?
        ) : UiState()
    }

    // Internal state
    private data class ViewModelState(
        val projects: Map<String, PyLeapProject> = mapOf(),
        val selectedProjectId: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val searchInput: String = ""
    ) {
        //private val log by LogUtils()

        fun toUiState(): UiState =
            if (isLoading) {
                UiState.Loading
            } else if (errorMessage != null) {
                UiState.Error(errorMessage)
            } else {
                UiState.Projects(
                    projects = projects.values.toList(),
                    selectedProject = if (selectedProjectId != null) projects[selectedProjectId] else null
                )
            }
    }

    // Data - Private
    private val viewModelState = MutableStateFlow(
        ViewModelState(
            projects = projectsRepository.projects.value,
            isLoading = true
        )
    )
    private val log by LogUtils()

    // Data
    val uiState = viewModelState
        .map {
            it.toUiState()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    // region Lifecycle
    init {
        // Monitor projects changes
        viewModelScope.launch {
            projectsRepository.projects.collect { projects ->
                log.info("Projects updated: ${projects.size}")

                // Update internal state
                viewModelState.update { viewModelState ->
                    viewModelState.copy(
                        projects = projects,
                        selectedProjectId = if (autoSelectFirstProjectAfterLoading) projects.values.firstOrNull()?.data?.id else null,
                        isLoading = false
                    )
                }
            }
        }

        // Monitor errors
        viewModelScope.launch {
            projectsRepository.networkLastException.collect { exception ->
                exception?.let {
                    log.info("Projects refresh error: $exception")

                    // Update internal state
                    viewModelState.update { viewModelState ->
                        viewModelState.copy(
                            errorMessage = "Can't get projects from server. Check your internet connection",
                            isLoading = false
                        )
                    }
                }
            }
        }

        // Load projects
        refreshProjects()
    }
    // endregion

    // region Actions
    fun refreshProjects() {
        // UI state is refreshing
        viewModelState.update { it.copy(isLoading = true) }

        // Refresh feed
        projectsRepository.refreshProjectFeed(externalScope = viewModelScope)
    }

    fun selectProjectId(id: String) {
        viewModelState.update {
            it.copy(selectedProjectId = id)
        }
    }

    fun unselectAll() {
        viewModelState.update {
            it.copy(selectedProjectId = null)
        }
    }

    fun runProjectId(id: String, fileTransferClient: FileTransferClient) {
        viewModelState.value.projects[id]?.let { project ->

            val transferState = project.transferState.value
            if (transferState is ProjectTransferStatus.Transferring) {
                log.info("No action: already transferring")
            } else when (project.downloadState.value) {
                is ProjectDownloadStatus.Connecting -> {
                    log.info("No action: already connecting")
                }
                is ProjectDownloadStatus.Downloading -> { /* Do nothing */
                    log.info("No action: already downloading")
                }
                ProjectDownloadStatus.Processing -> { /* Do nothing */
                    log.info("No action: already processing")
                }
                ProjectDownloadStatus.Downloaded -> {
                    project.setTransferState(ProjectTransferStatus.Transferring(0f))
                    transmit(
                        projectData = project.data,
                        fileTransferClient = fileTransferClient,
                        progress = { remainingFiles, totalFiles ->
                            val factor = (totalFiles - remainingFiles).coerceAtLeast(0) / totalFiles.toFloat()
                            project.setTransferState(ProjectTransferStatus.Transferring(factor))
                        },
                    ) { result ->
                        result.fold(
                            onSuccess = {
                                project.setTransferState(ProjectTransferStatus.Transferred)
                            },
                            onFailure = {
                                project.setTransferState(ProjectTransferStatus.Error(it))
                            },
                        )
                    }
                }
                else -> {
                    download(projectData = project.data)
                }
            }
        }
    }

    private data class FileToTransfer(
        val originFile: File,
        val destination: String,
        val isFolder: Boolean
    )


    private fun transmit(
        projectData: ProjectData,
        fileTransferClient: FileTransferClient,
        progress: (remainingFiles: Int, totalFiles: Int) -> Unit,
        completion: ((Result<Unit>) -> Unit)
    ) {
        val directory = projectsRepository.getFilesDirectory()
        if (directory == null) {
            log.info("${projectData.id} error getFilesDirectory null")
        } else {
            val originDirectoryFile = File(directory, projectData.id)

            getCircuitPythonVersionFromPeripheralBootFile(fileTransferClient = fileTransferClient) { pythonDirectory ->
                log.info("Use $pythonDirectory folder")

                val pythonDirectoryPath = pythonDirectory + File.separator

                val filesToTransfer: MutableList<FileToTransfer> = mutableListOf()
                originDirectoryFile.walkTopDown().forEach {
                    //log.info("File: ${it.path}")

                    if (!it.isHidden) {
                        val path = it.path
                        val pythonDirectoryStartingIndex = path.indexOf(pythonDirectoryPath)
                        if (pythonDirectoryStartingIndex >= 0) {
                            val pythonDirectoryEndingIndex =
                                pythonDirectoryStartingIndex + pythonDirectoryPath.utf8Size()
                            val destinationPath = path.substring(pythonDirectoryEndingIndex.toInt())

                            //log.info("File: $path -> $destinationPath")
                            filesToTransfer.add(
                                FileToTransfer(
                                    it,
                                    "/$destinationPath",
                                    it.isDirectory
                                )
                            )
                        }
                    }
                }

                /*
                filesToTransfer.forEach { fileToTransfer ->
                    log.info("Transfer ${if (fileToTransfer.isFolder) "Folder" else "File" } : ${fileToTransfer.origin} -> ${fileToTransfer.destination}")
                }*/

                if (filesToTransfer.isEmpty()) {
                    completion(Result.failure(Exception("No files to send")))
                } else {
                    transferFiles(
                        filesToTransfer,
                        fileTransferClient,
                        progress = { remainingFiles ->
                            progress(remainingFiles, filesToTransfer.size)
                        }) {
                        completion(it)
                    }
                }
            }
        }
    }

    private fun transferFiles(
        filesToTransfer: List<FileToTransfer>,
        fileTransferClient: FileTransferClient,
        progress: (remainingFiles: Int) -> Unit,
        completion: ((Result<Unit>) -> Unit)
    ) {
        val fileToTransfer = filesToTransfer.firstOrNull()
        progress(filesToTransfer.size)

        if (fileToTransfer == null) {
            completion(Result.success(Unit))
            return
        }

        if (fileToTransfer.isFolder) {
            fileTransferClient.makeDirectory(
                externalScope = viewModelScope,
                path = fileToTransfer.destination
            ) { result ->
                if (result.isFailure) {
                    log.warning("failed to make directory: ${fileToTransfer.destination}")
                }

                val remainingFilesToTransfer = filesToTransfer.drop(1)
                transferFiles(remainingFilesToTransfer, fileTransferClient, progress, completion)
            }

        } else {

            val byteArray = fileToTransfer.originFile.readBytes()
            fileTransferClient.writeFile(
                externalScope = viewModelScope,
                path = fileToTransfer.destination,
                data = byteArray,
                progress = null,
            ) { result ->
                result.fold(
                    onSuccess = {
                        // Continue removing the one that has been transferred
                        val remainingFilesToTransfer = filesToTransfer.drop(1)
                        transferFiles(remainingFilesToTransfer, fileTransferClient, progress, completion)
                    },
                    onFailure = {
                        log.warning("failed to write file: ${fileToTransfer.destination}. size: ${byteArray.size}. $it")
                        completion(Result.failure(it))
                    },
                )
            }
        }
    }

    private fun getCircuitPythonVersionFromPeripheralBootFile(
        fileTransferClient: FileTransferClient,
        completion: (String) -> Unit
    ) {

        val version7FolderName = "CircuitPython 7.x"
        val version8FolderName = "CircuitPython 8.x"
        val defaultResult = version8FolderName

        fileTransferClient.readFile(
            externalScope = viewModelScope,
            path = "/boot_out.txt",
            progress = null,
        ) { result ->
            result.fold(
                onSuccess = {
                    val isVersion7 = String(it).contains("CircuitPython 7")
                    completion(if (isVersion7) version7FolderName else defaultResult)
                },
                onFailure = {
                    completion(defaultResult)
                },
            )
        }
    }

    private fun download(projectData: ProjectData) {
        projectsRepository.downloadProject(
            projectData = projectData,
            externalScope = viewModelScope,
        )
    }
    // endregion

    /**
     * Factory that takes ProjectsRepository as a dependency
     */
    companion object {
        fun provideFactory(
            autoSelectFirstProjectAfterLoading: Boolean,
            projectsRepository: ProjectsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectsViewModel(
                    autoSelectFirstProjectAfterLoading,
                    projectsRepository
                ) as T
            }
        }
    }
}