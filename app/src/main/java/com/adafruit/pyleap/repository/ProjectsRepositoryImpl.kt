package com.adafruit.pyleap.repository

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.content.Context
import com.adafruit.glider.utils.LogUtils
import com.adafruit.pyleap.model.ProjectData
import com.adafruit.pyleap.model.ProjectDownloadStatus
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.network.NetworkService
import com.adafruit.pyleap.network.ResponseBodyDownloadStatus
import com.adafruit.pyleap.network.downloadToFileWithProgress
import com.adafruit.pyleap.utils.UnzipUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class ProjectsRepositoryImpl(private val context: Context) : ProjectsRepository {

    private val _projects: MutableStateFlow<Map<String, PyLeapProject>> = MutableStateFlow(mapOf())

    override val projects: StateFlow<Map<String, PyLeapProject>> = _projects.asStateFlow()
    private var downloadJobs: MutableMap<String, Job> =
        mutableMapOf()      // ProjectId -> DownloadService
    private val _networkLastException = MutableStateFlow<Exception?>(null)
    override val networkLastException = _networkLastException.asStateFlow()
    private val log by LogUtils()

    // region Feed
    override fun refreshProjectFeed(
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher
    ) {
        val networkService = NetworkService.getInstance(context)
        _networkLastException.update { null }

        externalScope.launch(defaultDispatcher) {
            try {
                val projectsData = networkService.getAllProjects().allProjects
                val pyLeapProjects = projectsData.map { projectData ->

                    val downloadState =
                        calculateProjectDownloadState(context = context, projectData = projectData)
                    PyLeapProject(data = projectData, downloadStatus = downloadState)
                }
                _projects.update { pyLeapProjects.associateBy({ it.data.id }, { it }) }
            } catch (exception: Exception) {
                _networkLastException.update { exception }
            }
        }
    }
    // endregion

    // region Download Project
    override fun downloadProject(
        projectData: ProjectData,
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher
    ) {
        val existingDownloadJob = downloadJobs[projectData.id]
        val isAlreadyDownloading = existingDownloadJob != null

        if (isAlreadyDownloading) {
            log.warning("Download already in-progress for ${projectData.title}")
            return
        }

        val directory = getFilesDirectory()
        if (directory == null) {
            log.info("${projectData.id} error getFilesDirectory null")
        } else {
            log.info("download for ${projectData.title} init")
            downloadJobs[projectData.id] = externalScope.launch(defaultDispatcher) {
                try {
                    updateProjectDownloadStatus(
                        id = projectData.id,
                        downloadStatus = ProjectDownloadStatus.Connecting
                    )

                    NetworkService.getInstance(context).getUrl(projectData.bundleUrl)
                        .downloadToFileWithProgress(directory, projectData.id)
                        .onStart {
                            log.info("download start for url: ${projectData.bundleUrl.path}")
                        }
                        .map { downloadStatus ->
                            when (downloadStatus) {
                                is ResponseBodyDownloadStatus.Progress -> {
                                    log.info("${projectData.id} progress: ${downloadStatus.value}")
                                    updateProjectDownloadStatus(
                                        id = projectData.id,
                                        downloadStatus = ProjectDownloadStatus.Downloading(progress = downloadStatus.value)
                                    )
                                }

                                is ResponseBodyDownloadStatus.Finished -> {
                                    log.info("${projectData.id} finished: ${downloadStatus.file.path}")
                                    updateProjectDownloadStatus(
                                        id = projectData.id,
                                        downloadStatus = ProjectDownloadStatus.Processing
                                    )

                                    // Unzip
                                    val destination = File(directory, projectData.id).path
                                    log.info("${projectData.id} unzip to: $destination")

                                    try {
                                        UnzipUtils.unzip(
                                            zipFilePath = downloadStatus.file,
                                            destDirectory = destination
                                        )

                                        updateProjectDownloadStatus(
                                            id = projectData.id,
                                            downloadStatus = ProjectDownloadStatus.Downloaded
                                        )
                                    } catch (e: Exception) {
                                        log.warning("${projectData.id} error unzipping: $e")
                                        updateProjectDownloadStatus(
                                            id = projectData.id,
                                            downloadStatus = ProjectDownloadStatus.Error(cause = e)
                                        )
                                    } finally {
                                        // Always delete the zip file
                                        downloadStatus.file.delete()
                                    }
                                }
                            }
                        }
                        .onCompletion {
                            log.info("download for ${projectData.title} onCompletion")
                            downloadJobs.remove(projectData.id)
                        }
                        .flowOn(defaultDispatcher)
                        .launchIn(externalScope)
                } catch (exception: Exception) {
                    _networkLastException.update { exception }
                }
            }
        }
    }

    private fun updateProjectDownloadStatus(id: String, downloadStatus: ProjectDownloadStatus) {
        _projects.update {
            val projects = it.toMutableMap()
            val project = it[id]
            if (project != null) {
                project.setDownloadState(downloadStatus)
                projects[id] = project

                projects
            } else {
                it
            }
        }
    }

    private fun calculateProjectDownloadState(
        context: Context, projectData: ProjectData
    ): ProjectDownloadStatus {

        val existingDownloadJob = downloadJobs[projectData.id]
        if (existingDownloadJob != null) {
            // TODO
            return ProjectDownloadStatus.Downloading(progress = 0f)
        } else {
            val directory = getFilesDirectory()
            val filename = projectData.id
            val file = File(directory, filename)

            val exists =
                if (file.exists()) ProjectDownloadStatus.Downloaded else ProjectDownloadStatus.NotDownloaded
            //log.info("${projectData.id} -> ${file.path} -> exists: ${file.exists()}")
            return exists
        }
    }

    override fun getFilesDirectory(): File? {
        return context.getExternalFilesDir(null)
    }

    // endregion

    // region Transmit Project
    override fun transmitProject(
        projectData: ProjectData,
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher
    ) {
        val directory = getFilesDirectory()
        if (directory == null) {
            log.info("${projectData.id} error getFilesDirectory null")
        } else {
            val origin = File(directory, projectData.id).path


        }
    }


    private fun getCircuitPythonVersionFromPeripheralBootFile() {

    }
    // endregion
}