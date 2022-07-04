package com.adafruit.pyleap.ui

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adafruit.pyleap.AppContainer
import com.adafruit.pyleap.ui.connection.ConnectionViewModel
import com.adafruit.pyleap.ui.projects.ProjectsScreen
import com.adafruit.pyleap.ui.projects.ProjectsViewModel
import com.adafruit.pyleap.ui.startup.StartupScreen
import com.adafruit.pyleap.ui.startup.StartupViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.openroad.ble.BleManager

@OptIn(ExperimentalPermissionsApi::class)

@Composable
fun PyLeapNavGraph(
    appContainer: AppContainer,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = PyLeapDestinations.Startup.route,
) {
    // Navigation Actions
    val navigationActions = remember(navController) {
        PyLeapNavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Startup
        composable(PyLeapDestinations.Startup.route) {

            val startupViewModel: StartupViewModel = viewModel(
                factory = StartupViewModel.provideFactory(
                    fileTransferConnectionManager = appContainer.filetransferConnectionManager,
                    onFinished = navigationActions.navigateToProjects,
                )
            )

            StartupScreen(startupViewModel = startupViewModel)
        }

        //  Projects
        composable(PyLeapDestinations.Projects.route) {
            val projectsViewModel: ProjectsViewModel = viewModel(
                factory = ProjectsViewModel.provideFactory(
                    autoselectFirstProjectAfterLoading =  isExpandedScreen,
                    projectsRepository = appContainer.projectsRepository
                )
            )

            val connectionViewModel: ConnectionViewModel = viewModel(
                factory = ConnectionViewModel.provideFactory(
                    isInPreviewMode = LocalInspectionMode.current,
                    bleStateRepository = appContainer.bleStateRepository,
                    fileTransferConnectionManager = appContainer.filetransferConnectionManager
                )
            )

            ProjectsScreen(
                projectsViewModel = projectsViewModel,
                connectionViewModel = connectionViewModel,
                isExpandedScreen = isExpandedScreen,
            )
        }
    }
}