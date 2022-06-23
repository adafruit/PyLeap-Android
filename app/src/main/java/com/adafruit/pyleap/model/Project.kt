package com.adafruit.pyleap.model

import android.net.Uri

/**
 * Created by Antonio García (antonio@openroad.es)
 */

data class Project(
    val title: String,
    val id: String = title,
    val imageUrl: Uri,
    val description: String,
    val bundleUrl: Uri,
    val learnGuideUrl: Uri,
    val compatibility: List<String>
)