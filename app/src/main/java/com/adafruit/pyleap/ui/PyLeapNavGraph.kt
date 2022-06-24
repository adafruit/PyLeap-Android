package com.adafruit.pyleap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adafruit.pyleap.AppContainer
import com.adafruit.pyleap.ui.projects.ProjectsScreen
import com.adafruit.pyleap.ui.projects.ProjectsViewModel

/**
 * Created by Antonio García (antonio@openroad.es)
 */

@Composable
fun PyLeapNavGraph(
    appContainer: AppContainer,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = PyLeapDestinations.Projects.route
    ) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(PyLeapDestinations.Projects.route) {
            val projectsViewModel: ProjectsViewModel = viewModel(
                factory = ProjectsViewModel.provideFactory(appContainer.projectsRepository)
            )

            ProjectsScreen(
                projectsViewModel = projectsViewModel,
                isExpandedScreen = isExpandedScreen,
            )
        }

    }
}