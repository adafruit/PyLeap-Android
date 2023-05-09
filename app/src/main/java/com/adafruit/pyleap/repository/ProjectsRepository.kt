package com.adafruit.pyleap.repository

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import com.adafruit.pyleap.model.ProjectData
import com.adafruit.pyleap.model.PyLeapProject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Interface to the Projects data layer.
 */
interface ProjectsRepository {
    // Projects
    val projects: StateFlow<Map<String, PyLeapProject>>
    val networkLastException: StateFlow<Exception?>

    // Actions
    fun refreshProjectFeed(
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    )

    fun downloadProject(
        projectData: ProjectData,
        externalScope: CoroutineScope,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    )

    fun getFilesDirectory(): File?
}