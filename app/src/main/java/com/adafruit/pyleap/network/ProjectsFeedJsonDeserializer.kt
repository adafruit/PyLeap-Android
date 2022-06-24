package com.adafruit.pyleap.network

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.net.Uri
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.model.ProjectsFeed
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class ProjectsFeedJsonDeserializer : JsonDeserializer<ProjectsFeed> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ProjectsFeed {
        json as JsonObject

        //val formatVersion = json["formatVersion"].asString
        val projectsJson = json["projects"].asJsonArray

        val projects = mutableListOf<Project>()
        for (projectJson in projectsJson) {
            projectJson as JsonObject
            val name = projectJson["projectName"].asString
            val imageUrl = Uri.parse(projectJson["projectImage"].asString)
            val description = projectJson["description"].asString
            val bundleUrl = Uri.parse(projectJson["bundleLink"].asString)
            val learnGuideUrl = Uri.parse(projectJson["learnGuideLink"].asString)
            val compatibilityListJson = projectJson["compatibility"].asJsonArray
            val compatibility = compatibilityListJson.map { it.asString }

            val project = Project(
                title = name,
                imageUrl = imageUrl,
                description = description,
                bundleUrl = bundleUrl,
                learnGuideUrl = learnGuideUrl,
                compatibility = compatibility
            )
            projects.add(project)
        }

        return ProjectsFeed(projects)
    }
}