package com.adafruit.pyleap.ui.projectdetails

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.adafruit.pyleap.R
import com.adafruit.pyleap.model.ProjectDownloadStatus
import com.adafruit.pyleap.model.ProjectTransferStatus
import com.adafruit.pyleap.model.PyLeapProject
import com.adafruit.pyleap.repository.ProjectsRepositoryFake
import com.adafruit.pyleap.ui.components.PyLeapSnackbarHost
import com.adafruit.pyleap.ui.connection.ConnectionCard
import com.adafruit.pyleap.ui.connection.PeripheralsDialog
import com.adafruit.pyleap.ui.connection.PeripheralsViewModel
import com.adafruit.pyleap.ui.theme.ConnectionStatusSuccess
import com.adafruit.pyleap.ui.theme.NavigationBackground
import com.adafruit.pyleap.ui.theme.ProjectCardBackground
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import io.openroad.filetransfer.ble.peripheral.BondedBlePeripherals
import io.openroad.filetransfer.ble.peripheral.BondedBlePeripheralsFake
import io.openroad.filetransfer.ble.scanner.BlePeripheralScannerFake
import io.openroad.filetransfer.filetransfer.ConnectionManager
import io.openroad.filetransfer.wifi.peripheral.SavedSettingsWifiPeripherals
import io.openroad.filetransfer.wifi.scanner.WifiPeripheralScannerFake
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProjectDetailsScreen(
    modifier: Modifier = Modifier,
    pyLeapProject: PyLeapProject,
    onBack: () -> Unit,
    onRunProjectId: (String) -> Unit,
    isExpandedScreen: Boolean,
    connectionManager: ConnectionManager,
    bondedBlePeripherals: BondedBlePeripherals? = null,          // only needed when !isExpandedScreen
    savedSettingsWifiPeripherals: SavedSettingsWifiPeripherals? = null,    // only needed when !isExpandedScreen
) {
    //val isConnectedToPeripheral by remember { derivedStateOf { connectionManager.currentFileTransferClient.value != null } }
    val currentFileTransferClient by connectionManager.currentFileTransferClient.collectAsState()
    val isConnectedToPeripheral = currentFileTransferClient != null

    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val showTopAppBar = !isExpandedScreen
    val project = pyLeapProject.data

    NavHost(
        navController = navController,
        startDestination = ProjectDetailsDestinations.Details.route
    ) {

        // Project Details
        composable(ProjectDetailsDestinations.Details.route) {
            var isScanDialogOpen by rememberSaveable { mutableStateOf(false) }

            Scaffold(
                snackbarHost = { PyLeapSnackbarHost(snackBarHostState) },
                topBar = {
                    if (showTopAppBar) {
                        ProjectDetailsTopBar(
                            title = pyLeapProject.data.title,
                            subtitle = null,
                            showBackButton = true,
                            onBack = onBack,
                        )
                    }
                }, modifier = modifier
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    if (!isExpandedScreen) {
                        ConnectionCard(
                            connectionManager = connectionManager,
                            bondedBlePeripherals = bondedBlePeripherals,
                            onOpenScanDialog = { isScanDialogOpen = true },
                        )
                    }

                    ProjectContents(
                        pyLeapProject = pyLeapProject,
                        showProjectTitle = !showTopAppBar,
                        onShowLearningGuide = { navController.navigate(ProjectDetailsDestinations.LearningGuide.route) },
                        isConnectedToPeripheral = isConnectedToPeripheral,
                        snackBarHostState = snackBarHostState,
                        onRunProjectId = {
                            if (/*isExpandedScreen || */isConnectedToPeripheral) {
                                onRunProjectId(it)
                            } else {    // Show scan dialog inside this screen
                                isScanDialogOpen = true
                            }
                        },
                    )
                }

                // Scan dialog
                if (/*!isExpandedScreen &&*/ isScanDialogOpen && bondedBlePeripherals != null && savedSettingsWifiPeripherals != null) {
                    val peripheralsViewModel: PeripheralsViewModel =
                        viewModel(
                            factory = PeripheralsViewModel.provideFactory(
                                connectionManager = connectionManager,
                                bondedBlePeripherals = bondedBlePeripherals,
                                savedSettingsWifiPeripherals = savedSettingsWifiPeripherals
                            )
                        )

                    PeripheralsDialog(
                        viewModel = peripheralsViewModel,
                        isExpandedScreen = isExpandedScreen,
                        onClose = { isScanDialogOpen = false })
                }
            }
        }

        // Learning Guide
        composable(ProjectDetailsDestinations.LearningGuide.route) {
            val state = rememberWebViewState(project.learnGuideUrl.toString())

            Scaffold(
                topBar = {
                    ProjectDetailsTopBar(
                        title = project.title,
                        subtitle = "Learning Guide".uppercase(),
                        showBackButton = true,
                        onBack = { navController.popBackStack() },
                    )
                }, modifier = modifier
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    WebView(
                        state = state,
                        onCreated = { webView ->
                            webView.settings.javaScriptEnabled = true
                        },
                    )
                }
            }

            /*
            BackHandler {
                navController.popBackStack()
            }*/
        }
    }
}

@Composable
private fun ProjectContents(
    pyLeapProject: PyLeapProject,
    showProjectTitle: Boolean,
    onShowLearningGuide: () -> Unit,
    isConnectedToPeripheral: Boolean,
    onRunProjectId: (String) -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val downloadState by pyLeapProject.downloadState.collectAsState()
    val transferState by pyLeapProject.transferState.collectAsState()

    LaunchedEffect(downloadState) {
        if (downloadState is ProjectDownloadStatus.Error) {
            snackBarHostState.showSnackbar(message = "Error: ${(downloadState as ProjectDownloadStatus.Error).cause.localizedMessage}")
        }
    }

    LaunchedEffect(transferState) {
        if (transferState is ProjectTransferStatus.Transferred) {
            snackBarHostState.showSnackbar(message = "${pyLeapProject.data.title} successfully transferred")
        } else if (transferState is ProjectTransferStatus.Error) {
            snackBarHostState.showSnackbar(message = "Error: ${(transferState as ProjectTransferStatus.Error).cause.localizedMessage}")
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        val project = pyLeapProject.data
        if (showProjectTitle) {
            Text(
                project.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        // Image
        val imageMaxHeight = 200.dp

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(project.imageUrl)
                .crossfade(true).build(),
            loading = {
                ImageLoading(maxHeight = imageMaxHeight)
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                //.fillMaxWidth()
                .heightIn(0.dp, imageMaxHeight)             // Limit max height to 200dp
                .fillMaxHeight()
        )

        // Details
        Text(
            project.description,
            //style = MaterialTheme.typography.bodyLarge,
        )

        // Compatibility
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Compatible with:", fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {

                val compatibilityStringResourceId = hashMapOf(
                    "circuitplayground_bluefruit" to R.string.compatiblity_circuitplayground_bluefruit,
                    "clue_nrf52840_express" to R.string.compatiblity_clue_nrf52840_express
                )

                project.compatibility.forEach { compatibleDevice ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = ConnectionStatusSuccess,
                        )

                        val textId = compatibilityStringResourceId.get(compatibleDevice)
                        Text(if (textId != null) stringResource(textId) else compatibleDevice)
                    }
                }
            }
        }

        // Actions
        /*Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {*/
        Column(
            //modifier = Modifier.widthIn(0.dp, 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onShowLearningGuide,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ProjectCardBackground,
                    //disabledContentColor = Color.Gray,
                ),
                border = BorderStroke(1.dp, ProjectCardBackground),
            ) {
                Text("Learn Guide")
            }

            ActionButton(
                transferState = transferState,
                downloadState = downloadState,
                isConnectedToPeripheral = isConnectedToPeripheral,
                onRunProjectId = { onRunProjectId(pyLeapProject.data.id) }
            )
        }
        //}
    }
}

@Composable
private fun ActionButton(
    transferState: ProjectTransferStatus,
    downloadState: ProjectDownloadStatus,
    isConnectedToPeripheral: Boolean,
    onRunProjectId: () -> Unit
) {
    val isTransferring = transferState is ProjectTransferStatus.Transferring
    val enabled =
        !isTransferring && (downloadState == ProjectDownloadStatus.NotDownloaded || downloadState is ProjectDownloadStatus.Error || downloadState == ProjectDownloadStatus.Downloaded)

    Button(
        onClick = { onRunProjectId() },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        if (isConnectedToPeripheral) {

            if (transferState is ProjectTransferStatus.Transferring) {
                Text("Transferring ${(transferState.progress * 100).roundToInt()}%...")
            } else when (downloadState) {
                ProjectDownloadStatus.Connecting -> {
                    Text("Connecting...")
                }
                is ProjectDownloadStatus.Downloading -> {
                    Text("Downloading... ${(downloadState.progress * 100).roundToInt()}%")
                }
                ProjectDownloadStatus.Processing -> {
                    Text("Processing...")
                }
                ProjectDownloadStatus.Downloaded -> {
                    Text("Run")
                }
                else -> {
                    // ProjectDownloadStatus.NotDownloaded
                    // is ProjectDownloadStatus.Error
                    Text("Download")
                }
            }
        } else {
            Text("Connect")
        }
    }
}

/*
@Composable
private fun StartUrlIntent(url: String) {
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(LocalContext.current, webIntent)
}*/

@Composable
private fun ImageLoading(maxHeight: Dp) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(Color.LightGray)
            .heightIn(0.dp, maxHeight)             // Limit max height to 200dp
            .fillMaxHeight(),        // Try to fill a 30% of height )
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ProjectCardBackground)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDetailsTopBar(
    title: String,
    subtitle: String?,
    showBackButton: Boolean,
    onBack: () -> Unit,
    //navigationIconContent: @Composable (() -> Unit) = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = NavigationBackground,
        ),
        title = {
            Column() {
                Text(title, color = Color.White)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        navigationIcon = if (showBackButton) {
            {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Navigate up",
                        tint = Color.White,
                    )
                }
            }
        } else {
            { /*Nothing*/ }
        }
    )
}

// region Preview
@Preview(showBackground = true)
@Composable
fun ProjectContentPreview() {
    // Use the FakeProjectsRepository for Preview
    val projects =
        ProjectsRepositoryFake(context = LocalContext.current).getAllPyLeapProjectsSynchronously()

    PyLeapTheme {
        ProjectContents(
            pyLeapProject = projects.first(),
            showProjectTitle = true,
            onShowLearningGuide = {},
            isConnectedToPeripheral = true,
            onRunProjectId = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectSmartphonePreview() {
    // Use the FakeProjectsRepository for Preview
    val projects =
        ProjectsRepositoryFake(context = LocalContext.current).getAllPyLeapProjectsSynchronously()

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    PyLeapTheme {
        ProjectDetailsScreen(pyLeapProject = projects.first(),
            isExpandedScreen = false,
            onBack = {},
            connectionManager = connectionManager,
            bondedBlePeripherals = BondedBlePeripheralsFake(),
            savedSettingsWifiPeripherals = SavedSettingsWifiPeripherals(LocalContext.current),
            //onShowLearningGuide = {},
            onRunProjectId = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectTabletPreview() {
    // Use the FakeProjectsRepository for Preview
    val projects =
        ProjectsRepositoryFake(context = LocalContext.current).getAllPyLeapProjectsSynchronously()

    val connectionManager = ConnectionManager(
        context = LocalContext.current,
        blePeripheralScanner = BlePeripheralScannerFake(),
        wifiPeripheralScanner = WifiPeripheralScannerFake(),
        onBlePeripheralBonded = { _, _ -> },
        onWifiPeripheralGetPasswordForHostName = { _, _ -> null }
    )

    PyLeapTheme {
        ProjectDetailsScreen(pyLeapProject = projects.first(),
            isExpandedScreen = true,
            onBack = {},
            connectionManager = connectionManager,
            bondedBlePeripherals = BondedBlePeripheralsFake(),
            savedSettingsWifiPeripherals = SavedSettingsWifiPeripherals(LocalContext.current),
            onRunProjectId = {})
    }
}

// endregion