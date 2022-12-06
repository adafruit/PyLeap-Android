package com.adafruit.pyleap.ui.connection

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.adafruit.pyleap.ui.components.PyLeapSnackbarHost
import com.adafruit.pyleap.ui.dialog.CustomDialog

/**
 * Created by Antonio García (antonio@openroad.es)
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeripheralsDialog(
    viewModel: PeripheralsViewModel,
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
) {
    CustomDialog(
        title = "Select Peripheral",
        isExpandedScreen = isExpandedScreen,
        onClose = onClose,
    ) {
        val snackBarHostState = remember { SnackbarHostState() }

        // Create scaffold to show snack-bar inside the dialog
        Scaffold(
            snackbarHost = { PyLeapSnackbarHost(snackBarHostState) },
        ) { innerPadding ->
            PeripheralsScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackBarHostState = snackBarHostState
            )
        }
    }
}


