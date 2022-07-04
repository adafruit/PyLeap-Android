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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.adafruit.pyleap.R
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.ui.about.AboutDialog
import com.adafruit.pyleap.ui.connection.ConnectionCard
import com.adafruit.pyleap.ui.connection.ConnectionViewModel
import com.adafruit.pyleap.ui.connection.ScanDialog
import com.adafruit.pyleap.ui.theme.NavigationBackground
import com.adafruit.pyleap.ui.utils.LoadingContent

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
fun ProjectsScaffold(
    modifier: Modifier = Modifier,
    uiState: ProjectsViewModel.UiState,
    isExpandedScreen: Boolean,
    onRefreshProjects: () -> Unit,
    //onSelectProjectId: (String) -> Unit,
    //projectsListLazyListState: LazyListState,
    connectionViewModel: ConnectionViewModel,
    projectsLoadedContent: @Composable (
        projects: List<Project>
    ) -> Unit
) {
    val showTopAppBar = true //!isExpandedScreen
    var isScanDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isAboutDialogOpen by rememberSaveable { mutableStateOf(false) }

    Box {

        // Main UI
        Scaffold(
            //snackbarHost = { PyLeapSnackbarHost(hostState = it) },
            topBar = {
                if (showTopAppBar) {
                    ProjectsAppBar(
                        connectionViewModel = connectionViewModel,
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
                    //onSelectProjectId = onSelectProjectId,
                    //projectsListLazyListState = projectsListLazyListState,
                    projectsLoadedContent = projectsLoadedContent
                )
            }
        }


        // Scan dialog
        if (isScanDialogOpen) {
            ScanDialog(
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
    //onSelectProjectId: (String) -> Unit,
    //projectsListLazyListState: LazyListState,
    projectsLoadedContent: @Composable (
        projects: List<Project>
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
            /*
            ProjectsList(
                projects = projects,
                onSelectProjectId = onSelectProjectId,
                projectsListLazyListState = projectsListLazyListState,
            )*/
        }
    }
}

@Composable
private fun ProjectsAppBar(
    connectionViewModel: ConnectionViewModel,
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
            connectionViewModel = connectionViewModel,
            onOpenScanDialog = onOpenScanDialog
        )
    }
}
