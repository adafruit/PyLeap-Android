package com.adafruit.pyleap.ui

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.adafruit.pyleap.AppContainer
import com.adafruit.pyleap.ui.theme.PyLeapTheme

@Composable
fun PyLeapApp(
    appContainer: AppContainer,
    widthSizeClass: WindowWidthSizeClass
) {
    PyLeapTheme {
        // Show standard NavGraph
        val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded
        val navController = rememberNavController()
        PyLeapNavGraph(
            appContainer = appContainer,
            isExpandedScreen = isExpandedScreen,
            navController = navController,
        )
    }
}

// -------------------- Old code (delete if not used) ------------------------------
/*
     val systemUiController = rememberSystemUiController()
     val darkIcons = MaterialTheme.colorScheme.isLight()
     SideEffect {
         systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = darkIcons)
     }*/
/*
val navigationActions = remember(navController) {
    PyLeapNavigationActions(navController)
}*/
/*
// Show special screen when Bluetooth is not available
val bleState by appContainer.bleStateRepository.bleState.collectAsState()

if (bleState != BleState.Enabled) {
    BluetoothStatusScreen(bleState = bleState)
}
else {*/

//}
