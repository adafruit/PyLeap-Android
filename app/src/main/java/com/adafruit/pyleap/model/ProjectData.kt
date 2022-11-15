package com.adafruit.pyleap.model

import android.net.Uri

/**
 * Created by Antonio García (antonio@openroad.es)
 */

data class ProjectData(
    val title: String,
    val imageUrl: Uri,
    val description: String,
    val bundleUrl: Uri,
    val learnGuideUrl: Uri,
    val compatibility: List<String>,
    val id: String = title + compatibility.joinToString(),      // TODO: create a proper identifier in the JSON file
)