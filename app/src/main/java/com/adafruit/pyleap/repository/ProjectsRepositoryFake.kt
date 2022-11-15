package com.adafruit.pyleap.repository

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.content.Context
import com.adafruit.pyleap.model.ProjectData
import com.adafruit.pyleap.model.ProjectDownloadStatus
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.network.ProjectsDataFeed
import com.adafruit.pyleap.network.ProjectsFeedJsonDeserializer
import com.adafruit.pyleap.utils.getDataFromAssetAsString
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class ProjectsRepositoryFake(private val context: Context) : ProjectsRepository {
    private val _projects: MutableStateFlow<Map<String, PyLeapProject>> = MutableStateFlow(mapOf())
    override val projects: StateFlow<Map<String, PyLeapProject>> = _projects.asStateFlow()
    private val _networkLastException = MutableStateFlow<Exception?>(null)
    override val networkLastException = _networkLastException.asStateFlow()

    // region Feed
    override fun refreshProjectFeed(
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher
    ) {
        val projectsData = getAllProjectsSynchronously().allProjects
        val pyLeapProjects = projectsData.map { projectData ->
            val downloadState =
                calculateProjectDownloadState(context = context, projectData = projectData)
            PyLeapProject(data = projectData, downloadStatus = downloadState)
        }
        _projects.update { pyLeapProjects.associateBy({ it.data.id }, { it }) }
    }
    // endregion

    // region Download Project
    override fun downloadProject(
        projectData: ProjectData, externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher
    ) {
        _networkLastException.update { Exception("Fake download failed") }
    }

    fun getAllPyLeapProjectsSynchronously(): List<PyLeapProject> {
        val projectsData = getAllProjectsSynchronously().allProjects
        val pyLeapProjects = projectsData.map { projectData ->
            val downloadState =
                calculateProjectDownloadState(context = context, projectData = projectData)
            PyLeapProject(data = projectData, downloadStatus = downloadState)
        }
        return pyLeapProjects
    }

    private fun getAllProjectsSynchronously(): ProjectsDataFeed {
        val jsonString =
            getDataFromAssetAsString(context = context, fileName = "pyleapProjects.json")

        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            ProjectsDataFeed::class.java, ProjectsFeedJsonDeserializer()
        )
        val gson = gsonBuilder.create()

        val projectsDataFeedType = object : TypeToken<ProjectsDataFeed>() {}.type
        val projectsDataFeed = gson.fromJson<ProjectsDataFeed>(jsonString, projectsDataFeedType)

        return projectsDataFeed
    }

    private fun calculateProjectDownloadState(
        context: Context, projectData: ProjectData
    ): ProjectDownloadStatus {
        val directory = getFilesDirectory()
        val filename = projectData.id
        val file = File(directory, filename)

        val exists =
            if (file.exists()) ProjectDownloadStatus.Downloaded else ProjectDownloadStatus.NotDownloaded
        //Log.d("test", "${projectData.id} -> ${file.path} -> exists: ${file.exists()}")
        return exists
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

    }
    // endregion
}