package com.adafruit.pyleap.ui

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Destinations used in the app
 */
sealed class PyLeapDestinations(val route: String) {
    object Startup : PyLeapDestinations("startup")
    object Projects : PyLeapDestinations("projects")
}

/**
 * Models the navigation actions in the app.
 */
class PyLeapNavigationActions(navController: NavHostController) {
    val navigateToProjects: () -> Unit = {
        navController.navigate(PyLeapDestinations.Projects.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // re-selecting the same item
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = true
        }
    }
}