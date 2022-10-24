package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.model.FakeProjectsRepositoryImpl
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.ui.projectdetails.ProjectDetailsScreen
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake

@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    projectsViewModel: ProjectsViewModel,
    connectionManager: ConnectionManager,
) {
    val uiState by projectsViewModel.uiState.collectAsState()

    ProjectsScreen(
        modifier = modifier,
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        projectsViewModel = projectsViewModel,
        connectionManager = connectionManager,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ProjectsScreen(
    modifier: Modifier = Modifier,
    uiState: ProjectsViewModel.UiState,
    isExpandedScreen: Boolean,
    projectsViewModel: ProjectsViewModel,
    connectionManager: ConnectionManager,
) {
    val projectsListLazyListState = rememberLazyListState()
    val projectsDetailLazyListStates = when (uiState) {
        is ProjectsViewModel.UiState.Projects -> uiState.projects
        else -> emptyList()
    }.associate { project ->
        key(project.id) {
            project.id to rememberLazyListState()
        }
    }

    val homeScreenType = getProjectsScreenType(isExpandedScreen, uiState)

    AnimatedContent(
        targetState = homeScreenType,
        transitionSpec = {
            // Compare the incoming number with the previous number.
            if (initialState == ProjectsScreenType.Feed && targetState is ProjectsScreenType.ArticleDetails) {
                slideInVertically { height -> height } + fadeIn() with
                        /*slideOutVertically { height -> -height } + */fadeOut()
            } else if (initialState is ProjectsScreenType.ArticleDetails && targetState == ProjectsScreenType.Feed) {
                /*slideInVertically { height -> -height } + */fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            } else {
                fadeIn() with fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        }
    ) { animatedHomeScreenType ->

        when (animatedHomeScreenType) {
            ProjectsScreenType.Feed -> {
                ProjectsScaffold(
                    modifier = modifier,
                    uiState = uiState,
                    isExpandedScreen = isExpandedScreen,
                    onRefreshProjects = { projectsViewModel.refreshProjects() },
                    connectionManager = connectionManager,
                ) { projects ->
                    ProjectsList(
                        projects = projects,
                        onSelectProjectId = { projectsViewModel.selectProjectId(it) },
                        projectsListLazyListState = projectsListLazyListState,
                    )
                }
            }

            is ProjectsScreenType.ArticleDetails -> {
                ProjectDetailsScreen(
                    modifier = modifier,
                    project = animatedHomeScreenType.project,
                    isExpandedScreen = isExpandedScreen,
                    onBack = { projectsViewModel.unselectAll() },
                    onRunProjectId = { /* TODO */ },
                )

                BackHandler {
                    projectsViewModel.unselectAll()
                }
            }

            is ProjectsScreenType.FeedWithArticleDetails -> {
                ProjectsScaffold(
                    modifier = modifier,
                    uiState = uiState,
                    isExpandedScreen = isExpandedScreen,
                    onRefreshProjects = { projectsViewModel.refreshProjects() },
                    connectionManager = connectionManager,
                ) { projects ->
                    check(uiState is ProjectsViewModel.UiState.Projects)

                    ProjectsWithDetails(
                        projects = projects,
                        selectedProject = animatedHomeScreenType.selectedProject,
                        onSelectProjectId = { projectsViewModel.selectProjectId(it) },
                        projectsListLazyListState = projectsListLazyListState,
                        projectsDetailLazyListStates = projectsDetailLazyListStates,
                    )
                }
            }
        }
    }
}

// region ProjectsScreenType

private sealed class ProjectsScreenType {
    object Feed : ProjectsScreenType()
    data class ArticleDetails(val project: Project) : ProjectsScreenType()
    data class FeedWithArticleDetails(val selectedProject: Project?) : ProjectsScreenType()
}

/**
 * Returns the current [ProjectsScreenType] to display, based on whether or not the screen is expanded
 * and the [ProjectsViewModel.UiState].
 */
@Composable
private fun getProjectsScreenType(
    isExpandedScreen: Boolean,
    uiState: ProjectsViewModel.UiState
): ProjectsScreenType = when (isExpandedScreen) {
    //true -> HomeScreenType.FeedWithArticleDetails(selectedProject = null)
    true -> {
        when (uiState) {
            is ProjectsViewModel.UiState.Projects -> {
                ProjectsScreenType.FeedWithArticleDetails(selectedProject = uiState.selectedProject)
            }
            else -> ProjectsScreenType.FeedWithArticleDetails(selectedProject = null)
        }
    }
    false -> {
        when (uiState) {
            is ProjectsViewModel.UiState.Projects -> {
                if (uiState.selectedProject != null) {
                    ProjectsScreenType.ArticleDetails(project = uiState.selectedProject)
                } else {
                    ProjectsScreenType.Feed
                }
            }
            else -> ProjectsScreenType.Feed
        }
    }
}
// endregion


// region Preview
@Preview(showBackground = true)
@Composable
fun ProjectsSmartphonePreview() {
    // Use the FakeProjectsRepository for Preview
    val projectsViewModel: ProjectsViewModel = viewModel(
        factory = ProjectsViewModel.provideFactory(
            false,
            FakeProjectsRepositoryImpl(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            connectionManager = connectionManager,
            isExpandedScreen = false
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_7_2013)
@Composable
fun ProjectsTabletPortraitPreview() {
    // Use the FakeProjectsRepository for Preview
    val projectsViewModel: ProjectsViewModel = viewModel(
        factory = ProjectsViewModel.provideFactory(
            false,
            FakeProjectsRepositoryImpl(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            connectionManager = connectionManager,
            isExpandedScreen = false
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun ProjectsTabletLandscapePreview() {
    // Use the FakeProjectsRepository for Preview
    val projectsViewModel: ProjectsViewModel = viewModel(
        factory = ProjectsViewModel.provideFactory(
            true,
            FakeProjectsRepositoryImpl(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    projectsViewModel.selectProjectId("Eyelights LED Glasses")

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            connectionManager = connectionManager,
            isExpandedScreen = true
        )
    }
}
// endregion