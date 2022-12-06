package com.adafruit.pyleap.network

import com.adafruit.pyleap.model.ProjectData

/**
 * Created by Antonio García (antonio@openroad.es)
 */


/*
    Used to deserialize project feed from JSON file
 */
data class ProjectsDataFeed(
    val allProjects: List<ProjectData>
)