package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.R
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.ui.about.AboutDialog
import com.adafruit.pyleap.ui.connection.ConnectionCard
import com.adafruit.pyleap.ui.connection.PeripheralsDialog
import com.adafruit.pyleap.ui.connection.PeripheralsViewModel
import com.adafruit.pyleap.ui.theme.NavigationBackground
import com.adafruit.pyleap.ui.utils.LoadingContent
import io.openroad.filetransfer.ble.peripheral.SavedBondedBlePeripherals
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.peripheral.SavedSettingsWifiPeripherals

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
fun ProjectsScaffold(
    modifier: Modifier = Modifier,
    uiState: ProjectsViewModel.UiState,
    isExpandedScreen: Boolean,
    onRefreshProjects: () -> Unit,
    connectionManager: ConnectionManager,
    savedBondedBlePeripherals: SavedBondedBlePeripherals,
    savedSettingsWifiPeripherals: SavedSettingsWifiPeripherals,
    projectsLoadedContent: @Composable (
        projects: List<PyLeapProject>
    ) -> Unit
) {
    val showTopAppBar = true //!isExpandedScreen
    var isScanDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isAboutDialogOpen by rememberSaveable { mutableStateOf(false) }

    Box {

        // Main UI
        Scaffold(
            topBar = {
                if (showTopAppBar) {
                    ProjectsAppBar(
                        connectionManager = connectionManager,
                        onOpenAbout = { isAboutDialogOpen = true },
                        onOpenScanDialog = { isScanDialogOpen = true })
                }
            },
            modifier = modifier
        ) { innerPadding ->

            Box(modifier = Modifier.padding(innerPadding)) {

                ProjectsContents(
                    uiState = uiState,
                    onRefreshProjects = onRefreshProjects,
                    projectsLoadedContent = projectsLoadedContent
                )
            }
        }

        // Scan dialog
        if (isScanDialogOpen) {
            val peripheralsViewModel: PeripheralsViewModel =
                viewModel(
                    factory = PeripheralsViewModel.provideFactory(
                        connectionManager = connectionManager,
                        savedBondedBlePeripherals = savedBondedBlePeripherals,
                        savedSettingsWifiPeripherals = savedSettingsWifiPeripherals
                    )
                )

            PeripheralsDialog(
                viewModel = peripheralsViewModel,
                isExpandedScreen = isExpandedScreen,
                onClose = { isScanDialogOpen = false })

        } else if (isAboutDialogOpen) {
            AboutDialog(
                isExpandedScreen = isExpandedScreen,
                onClose = { isAboutDialogOpen = false })
        }


        /* Animation not working
        AnimatedVisibility(
            visible = isScanDialogOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
                ScanDialog(
                    isExpandedScreen = isExpandedScreen,
                    onClose = { isScanDialogOpen = false })
        }
        */
        /* Animation not working
        AnimatedContent(
            targetState = isScanDialogOpen,
            transitionSpec = {
                // Compare the incoming number with the previous number.
                if (targetState) {
                    slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()
                }.using(
                    // Disable clipping since the faded slide-in/out should
                    // be displayed out of bounds.
                    SizeTransform(clip = false)
                )
            }
        ) { animatedIsScanDialogOpen ->

            // Scan dialog
            if (animatedIsScanDialogOpen) {
                //Box(Modifier.fillMaxSize()) {
                ScanDialog(
                    isExpandedScreen = isExpandedScreen,
                    onClose = { isScanDialogOpen = false })
            }
            //}
        }*/
    }
}

@Composable
private fun ProjectsContents(
    uiState: ProjectsViewModel.UiState,
    onRefreshProjects: () -> Unit,
    projectsLoadedContent: @Composable (
        projects: List<PyLeapProject>
    ) -> Unit
) {
    val isLoading = uiState == ProjectsViewModel.UiState.Loading
    val isEmpty = (uiState is ProjectsViewModel.UiState.Projects && uiState.projects.isEmpty())

    LoadingContent(
        empty = isLoading || isEmpty,
        emptyContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("No projects available")
                }
            }
        },
        loading = isLoading,
        onRefresh = { onRefreshProjects() },
    ) {
        if (uiState is ProjectsViewModel.UiState.Projects) {
            val projects = uiState.projects

            projectsLoadedContent(
                projects = projects,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectsAppBar(
    connectionManager: ConnectionManager,
    onOpenAbout: () -> Unit,
    onOpenScanDialog: () -> Unit,
) {
    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = NavigationBackground,
            ),
            title = {
                Image(
                    painter = painterResource(id = R.drawable.topbar_logo),
                    contentDescription = null, // decorative element
                    colorFilter = ColorFilter.tint(
                        Color.White,
                    ),
                )
            },
            actions = {
                IconButton(onClick = onOpenAbout) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(R.string.navigation_info)
                    )
                }
            }
        )

        ConnectionCard(
            connectionManager = connectionManager,
            onOpenScanDialog = onOpenScanDialog
        )
    }
}
