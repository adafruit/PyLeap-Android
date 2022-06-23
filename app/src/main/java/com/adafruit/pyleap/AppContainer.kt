package com.adafruit.pyleap

import android.content.Context
import com.adafruit.pyleap.model.ProjectsRepository
import com.adafruit.pyleap.model.ProjectsRepositoryImpl

/**
 * Created by Antonio García (antonio@openroad.es)
 */

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val projectsRepository: ProjectsRepository
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    override val projectsRepository: ProjectsRepository by lazy {
        ProjectsRepositoryImpl(context = applicationContext)
    }
}
