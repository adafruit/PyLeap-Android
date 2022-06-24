package com.adafruit.pyleap.ui.projectdetails

/**
 * Created by Antonio García (antonio@openroad.es)
 */

sealed class ProjectDetailsDestinations(val route: String) {
    object Details : ProjectDetailsDestinations("details")
    object LearningGuide : ProjectDetailsDestinations("learning_guide")
}

