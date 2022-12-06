package com.adafruit.pyleap.ui.dialog

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adafruit.pyleap.ui.connection.ScanDestinations
import com.adafruit.pyleap.ui.theme.NavigationBackground

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomDialog(
    title: String,
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = isExpandedScreen),
        onDismissRequest = onClose,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = if (isExpandedScreen) RoundedCornerShape(16.dp) else RectangleShape,
            //color = Color.LightGray
        ) {
            CustomDialogNavHost(
                title = title,
                isExpandedScreen = isExpandedScreen,
                onClose = onClose,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDialogNavHost(
    modifier: Modifier = Modifier,
    title: String,
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
    ) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.fillMaxWidth(0.5f),
        navController = navController, startDestination = ScanDestinations.Scan.route
    ) {
        composable(ScanDestinations.Scan.route) {
            val showTopAppBar = !isExpandedScreen

            Scaffold(
                topBar = {
                    if (showTopAppBar) {
                        DialogTopBar(
                            title = title,
                            isExpandedScreen = isExpandedScreen,
                            onClose = onClose,
                        )
                    }
                }, modifier = modifier
            ) { innerPadding ->

                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        DialogTopBar(
                            title = title,
                            isExpandedScreen = isExpandedScreen,
                            onClose = onClose,
                        )
                    }

                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTopBar(
    title: String,
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
) {
    val containerColor = NavigationBackground// if (!isExpandedScreen) NavigationBackground else Color.White
    val foregroundColor = Color.White//if (isExpandedScreen) Color.Black else Color.White

    TopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = containerColor,
    ), title = {
        Column() {
            Text(title, color = foregroundColor)
        }
    }, navigationIcon = {
        if (!isExpandedScreen) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close dialog",
                    tint = foregroundColor,
                )
            }
        }
    }, actions = {
        if (isExpandedScreen) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close dialog",
                    tint = foregroundColor,
                )
            }
        }
    })
}
