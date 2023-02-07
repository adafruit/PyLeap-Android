package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.repository.ProjectsRepositoryFake
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.ui.projectdetails.ProjectDetailsScreen
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import com.adafruit.pyleap.utils.observeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.openroad.filetransfer.Config
import io.openroad.filetransfer.ble.peripheral.BondedBlePeripherals
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.peripheral.SavedSettingsWifiPeripherals
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    projectsViewModel: ProjectsViewModel,
    scanViewModel: ScanViewModel,
    connectionManager: ConnectionManager,
    bondedBlePeripherals: BondedBlePeripherals,
    savedSettingsWifiPeripherals: SavedSettingsWifiPeripherals,
) {

    // Permissions
    val isInitialPermissionsCheckInProgress: Boolean
    if (LocalInspectionMode.current) {
        // Simulate permissions for Compose Preview
        isInitialPermissionsCheckInProgress = false
    } else {
        // Check Bluetooth-related permissions state
        val bluetoothPermissionState =
            rememberMultiplePermissionsState(Config.getNeededPermissions())

        isInitialPermissionsCheckInProgress =
            !bluetoothPermissionState.allPermissionsGranted && !bluetoothPermissionState.shouldShowRationale
        LaunchedEffect(isInitialPermissionsCheckInProgress) {
            if (isInitialPermissionsCheckInProgress) {
                // First time that permissions are needed at startup
                bluetoothPermissionState.launchMultiplePermissionRequest()
            } else {
                // Permissions ready
            }
        }
    }

    // Start / Stop scanning based on lifecycle
    val lifeCycleState = LocalLifecycleOwner.current.lifecycle.observeAsState()
    if (!isInitialPermissionsCheckInProgress && lifeCycleState.value == Lifecycle.Event.ON_RESUME) {
        LaunchedEffect(lifeCycleState) {
            scanViewModel.onResume()
        }
    } else if (lifeCycleState.value == Lifecycle.Event.ON_PAUSE) {
        LaunchedEffect(lifeCycleState) {
            scanViewModel.onPause()
        }
    }

    // UI
    val uiState by projectsViewModel.uiState.collectAsState()
    ProjectsScreen(
        modifier = modifier,
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        projectsViewModel = projectsViewModel,
        connectionManager = connectionManager,
        bondedBlePeripherals = bondedBlePeripherals,
        savedSettingsWifiPeripherals = savedSettingsWifiPeripherals,
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
    bondedBlePeripherals: BondedBlePeripherals,
    savedSettingsWifiPeripherals: SavedSettingsWifiPeripherals,
) {
    // Project List
    val projectsListLazyListState = rememberLazyListState()
    /*
    val projectsDetailLazyListStates = when (uiState) {
        is ProjectsViewModel.UiState.Projects -> uiState.projects
        else -> emptyList()
    }.associate { project ->
        key(project.data.id) {
            project.data.id to rememberLazyListState()
        }
    }*/

    val isLoadingProjects = uiState is ProjectsViewModel.UiState.Loading
    val homeScreenType = getProjectsScreenType(isExpandedScreen, uiState)

    val fileTransferClient by
    connectionManager.currentFileTransferClient.collectAsState()

    fun onRunProjectId(id: String) {
        fileTransferClient?.let {
            projectsViewModel.runProjectId(id = id, fileTransferClient = it)
        }
    }

    AnimatedContent(
        targetState = homeScreenType,
        transitionSpec = {
            // Compare the incoming number with the previous number.
            if (initialState == ProjectsScreenType.Feed && targetState is ProjectsScreenType.Details) {
                slideInVertically { height -> height } + fadeIn() with
                        /*slideOutVertically { height -> -height } + */fadeOut()
            } else if (initialState is ProjectsScreenType.Details && targetState == ProjectsScreenType.Feed) {
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
                    bondedBlePeripherals = bondedBlePeripherals,
                    savedSettingsWifiPeripherals = savedSettingsWifiPeripherals,
                ) { projects ->
                    ProjectsList(
                        projects = projects,
                        isLoading = isLoadingProjects,
                        onSelectProjectId = { projectsViewModel.selectProjectId(it) },
                        projectsListLazyListState = projectsListLazyListState,
                    )
                }
            }

            is ProjectsScreenType.Details -> {
                ProjectDetailsScreen(
                    modifier = modifier,
                    pyLeapProject = animatedHomeScreenType.project,
                    isExpandedScreen = isExpandedScreen,
                    onBack = { projectsViewModel.unselectAll() },
                    connectionManager = connectionManager,
                    bondedBlePeripherals = bondedBlePeripherals,
                    savedSettingsWifiPeripherals = savedSettingsWifiPeripherals,
                    onRunProjectId = { onRunProjectId(id = it) },
                )

                BackHandler {
                    projectsViewModel.unselectAll()
                }
            }

            is ProjectsScreenType.FeedWithDetails -> {
                ProjectsScaffold(
                    modifier = modifier,
                    uiState = uiState,
                    isExpandedScreen = isExpandedScreen,
                    onRefreshProjects = { projectsViewModel.refreshProjects() },
                    connectionManager = connectionManager,
                    bondedBlePeripherals = bondedBlePeripherals,
                    savedSettingsWifiPeripherals = savedSettingsWifiPeripherals,
                    //snackBarHostState = snackBarHostState,
                ) { projects ->
                    check(uiState is ProjectsViewModel.UiState.Projects)

                    ProjectsWithDetails(
                        connectionManager = connectionManager,
                        projects = projects,
                        isLoadingProjects = isLoadingProjects,
                        selectedProject = animatedHomeScreenType.selectedProject,
                        onSelectProjectId = { projectsViewModel.selectProjectId(it) },
                        onRunProjectId = { onRunProjectId(id = it) },
                        projectsListLazyListState = projectsListLazyListState,
                        //projectsDetailLazyListStates = projectsDetailLazyListStates,
                    )
                }
            }
        }
    }
}

// region ProjectsScreenType

private sealed class ProjectsScreenType {
    object Feed : ProjectsScreenType()
    data class Details(val project: PyLeapProject) : ProjectsScreenType()
    data class FeedWithDetails(val selectedProject: PyLeapProject?) : ProjectsScreenType()
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
                ProjectsScreenType.FeedWithDetails(selectedProject = uiState.selectedProject)
            }
            else -> ProjectsScreenType.FeedWithDetails(selectedProject = null)
        }
    }
    false -> {
        when (uiState) {
            is ProjectsViewModel.UiState.Projects -> {
                if (uiState.selectedProject != null) {
                    ProjectsScreenType.Details(project = uiState.selectedProject)
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
            ProjectsRepositoryFake(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    val scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModel.provideFactory(
            connectionManager = connectionManager
        )
    )

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            isExpandedScreen = false,
            scanViewModel = scanViewModel,
            connectionManager = connectionManager,
            bondedBlePeripherals = BondedBlePeripherals(LocalContext.current),
            savedSettingsWifiPeripherals = SavedSettingsWifiPeripherals(LocalContext.current)
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
            ProjectsRepositoryFake(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    val scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModel.provideFactory(
            connectionManager = connectionManager
        )
    )

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            isExpandedScreen = false,
            scanViewModel = scanViewModel,
            connectionManager = connectionManager,
            bondedBlePeripherals = BondedBlePeripherals(LocalContext.current),
            savedSettingsWifiPeripherals = SavedSettingsWifiPeripherals(LocalContext.current)
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
            ProjectsRepositoryFake(context = LocalContext.current)
        )
    )

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    val scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModel.provideFactory(
            connectionManager = connectionManager
        )
    )

    projectsViewModel.selectProjectId("Eyelights LED Glasses")

    PyLeapTheme {
        ProjectsScreen(
            projectsViewModel = projectsViewModel,
            isExpandedScreen = true,
            scanViewModel = scanViewModel,
            connectionManager = connectionManager,
            bondedBlePeripherals = BondedBlePeripherals(LocalContext.current),
            savedSettingsWifiPeripherals = SavedSettingsWifiPeripherals(LocalContext.current)
        )
    }
}
// endregion