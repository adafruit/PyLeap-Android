package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.repository.ProjectsRepositoryFake
import com.adafruit.pyleap.ui.projectdetails.ProjectDetailsScreen
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake

@Composable
fun ProjectsWithDetails(
    connectionManager: ConnectionManager,
    projects: List<PyLeapProject>,
    isLoadingProjects: Boolean,
    selectedProject: PyLeapProject?,
    onSelectProjectId: (String) -> Unit,
    onRunProjectId: (String) -> Unit,
    projectsListLazyListState: LazyListState = rememberLazyListState(),
    //  projectsDetailLazyListStates: Map<String, LazyListState>,
) {
    Row(
        //modifier = Modifier.padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        ProjectsList(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                //.notifyInput(onUnselectAll)
                .imePadding(), // add padding for the on-screen keyboard
            projects = projects,
            isLoading = isLoadingProjects,
            onSelectProjectId = onSelectProjectId,
            projectsListLazyListState = projectsListLazyListState,
        )

        // Cross-fade between different project details
        Crossfade(targetState = selectedProject) { project ->

            if (project == null) {
                /* TODO empty state */
            } else {
                // Get the lazy list state for this detail view
                /*
                val detailLazyListState by derivedStateOf {
                    projectsDetailLazyListStates.getValue(project.id)
                }
*/
                // Key against the project id to avoid sharing any state between different projects
                key(project.data.id) {
                    ProjectDetailsScreen(
                        pyLeapProject = project,
                        isExpandedScreen = true,
                        onBack = { /* Nothing to do */ },
                        onRunProjectId = onRunProjectId,
                        connectionManager = connectionManager,
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun ProjectsWithDetailsPreview() {
    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    // Use the ProjectsRepositoryFake for Preview
    val projects =
        ProjectsRepositoryFake(context = LocalContext.current).getAllPyLeapProjectsSynchronously()

    PyLeapTheme {
        ProjectsWithDetails(
            connectionManager = connectionManager,
            projects = projects,
            isLoadingProjects = false,
            selectedProject = projects.first(),
            onSelectProjectId = {},
            onRunProjectId = {},
        )
    }
}
// endregion