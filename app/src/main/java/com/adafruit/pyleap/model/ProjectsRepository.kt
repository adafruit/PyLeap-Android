package com.adafruit.pyleap.model

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.content.Context
import com.adafruit.pyleap.network.NetworkService
import com.adafruit.pyleap.network.ProjectsFeedJsonDeserializer
import com.adafruit.pyleap.utils.getDataFromAssetAsString
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * Interface to the Projects data layer.
 */
interface ProjectsRepository {
    suspend fun getAllProjects(): Result<ProjectsFeed>
}

class ProjectsRepositoryImpl(private val context: Context) : ProjectsRepository {
    override suspend fun getAllProjects(): Result<ProjectsFeed> {
        val networkService = NetworkService.getInstance(context)
        return try {
            val projects = networkService.getAllProjects()
            Result.success(projects)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}

class FakeProjectsRepositoryImpl(private val context: Context): ProjectsRepository {

    override suspend fun getAllProjects(): Result<ProjectsFeed> {
        val projectsFeed = getAllProjectsSynchronously()
        return Result.success(projectsFeed)
    }

    fun getAllProjectsSynchronously(): ProjectsFeed {
        val jsonString = getDataFromAssetAsString(context = context, fileName = "pyleapProjects.json")

        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(ProjectsFeed::class.java, ProjectsFeedJsonDeserializer())
        val gson = gsonBuilder.create()

        val projectsFeedType = object : TypeToken<ProjectsFeed>() {}.type
        val projectsFeed = gson.fromJson<ProjectsFeed>(jsonString, projectsFeedType)

        return projectsFeed
    }
}