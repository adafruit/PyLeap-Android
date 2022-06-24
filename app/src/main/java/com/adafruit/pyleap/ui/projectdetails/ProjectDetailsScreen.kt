package com.adafruit.pyleap.ui.projects

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
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.adafruit.pyleap.R
import com.adafruit.pyleap.model.FakeProjectsRepositoryImpl
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.ui.projectdetails.ProjectDetailsDestinations
import com.adafruit.pyleap.ui.theme.ConnectionStatusSuccess
import com.adafruit.pyleap.ui.theme.NavigationBackground
import com.adafruit.pyleap.ui.theme.ProjectCardBackground
import com.adafruit.pyleap.ui.theme.PyLeapTheme
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProjectDetailsScreen(
    modifier: Modifier = Modifier,
    project: Project,
    isExpandedScreen: Boolean,
    onBack: () -> Unit,
    onRunProjectId: (String) -> Unit
) {
    val navController = rememberNavController()
    val showTopAppBar = !isExpandedScreen

    NavHost(
        navController = navController,
        startDestination = ProjectDetailsDestinations.Details.route
    ) {
        composable(ProjectDetailsDestinations.Details.route) {
            Scaffold(
                topBar = {
                    if (showTopAppBar) {
                        ProjectDetailsTopBar(
                            title = project.title,
                            subtitle = null,
                            showBackButton = true,
                            onBack = onBack,
                        )
                    }
                }, modifier = modifier
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    ProjectContents(
                        project = project,
                        showProjectTitle = !showTopAppBar,
                        onShowLearningGuide = { navController.navigate(ProjectDetailsDestinations.LearningGuide.route) },
                        onRunProjectId = onRunProjectId,
                    )
                }
            }
        }

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
    project: Project,
    showProjectTitle: Boolean,
    onShowLearningGuide: () -> Unit,
    onRunProjectId: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

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
                        if (textId != null) {
                            Text(stringResource(id = textId))
                        }
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

            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect")
            }
        }
        //}
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

@Composable
private fun ProjectDetailsTopBar(
    title: String,
    subtitle: String?,
    showBackButton: Boolean,
    onBack: () -> Unit,
    //navigationIconContent: @Composable (() -> Unit) = {}
) {
    SmallTopAppBar(
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
fun ProjectSmartphonePreview() {
    // Use the FakeProjectsRepository for Preview
    val projects =
        FakeProjectsRepositoryImpl(context = LocalContext.current).getAllProjectsSynchronously()

    PyLeapTheme {
        ProjectDetailsScreen(project = projects.allProjects.first(),
            isExpandedScreen = false,
            onBack = {},
            //onShowLearningGuide = {},
            onRunProjectId = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectTabletPreview() {
    // Use the FakeProjectsRepository for Preview
    val projects =
        FakeProjectsRepositoryImpl(context = LocalContext.current).getAllProjectsSynchronously()

    PyLeapTheme {
        ProjectDetailsScreen(project = projects.allProjects.first(),
            isExpandedScreen = true,
            onBack = {},
            //onShowLearningGuide = {},
            onRunProjectId = {})
    }
}

// endregion