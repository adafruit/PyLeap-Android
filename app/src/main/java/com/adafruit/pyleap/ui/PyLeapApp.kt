package com.adafruit.pyleap

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.adafruit.pyleap.ui.PyLeapNavGraph
import com.adafruit.pyleap.ui.theme.PyLeapTheme

/**
 * Created by Antonio García (antonio@openroad.es)
 */

@Composable
fun PyLeapApp(
    appContainer: AppContainer,
    widthSizeClass: WindowWidthSizeClass
) {
    PyLeapTheme {
        /*
        val systemUiController = rememberSystemUiController()
        val darkIcons = MaterialTheme.colorScheme.isLight()
        SideEffect {
            systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = darkIcons)
        }*/

        val navController = rememberNavController()
        /*
        val navigationActions = remember(navController) {
            PyLeapNavigationActions(navController)
        }*/

        val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded

        /*
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

        }*/

        PyLeapNavGraph(
            appContainer = appContainer,
            isExpandedScreen = isExpandedScreen,
            navController = navController,
        )
    }
}


