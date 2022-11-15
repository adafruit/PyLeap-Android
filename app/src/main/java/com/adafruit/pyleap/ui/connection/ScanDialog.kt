package com.adafruit.pyleap.ui.connection

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.R
import com.adafruit.pyleap.ui.dialog.CustomDialog
import com.adafruit.pyleap.ui.theme.PyLeapTheme

@Composable
fun ScanDialog(
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
) {
    CustomDialog(
        title = "Select Peripheral",
        isExpandedScreen = isExpandedScreen,
        onClose = onClose,
    ) {
        ScanContents(isExpandedScreen = isExpandedScreen)
    }
}

@Composable
private fun ScanContents(
    isExpandedScreen: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.topbar_logo),
                contentDescription = null, // decorative element
            )

            /*
            Text(
                "Searching for PyLeap compatible device",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )*/
        }


        // Blinka rotating animation
        val infiniteTransition = rememberInfiniteTransition()
        val angle by infiniteTransition.animateFloat(
            initialValue = 0F, targetValue = 360F, animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing)
            )
        )

        Image(
            modifier = Modifier.graphicsLayer {
                rotationZ = angle
            },//.alpha(0.3f),
            painter = painterResource(id = R.drawable.scan_scanning),
            contentDescription = null, // decorative element
        )

        // Status
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Scanning...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pairing Tutorial")
            }
        }
    }
}

// region Previews
@Preview(showBackground = true)
@Composable
fun ScanSmartphonePreview() {
    PyLeapTheme {
        /*
        ScanDialog(
            isExpandedScreen = false,
            onClose = {},
        )*/
        
        ScanContents(isExpandedScreen = false)
    }
}
/*
@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
fun ScanTabletPreview() {
    PyLeapTheme {

        ScanDialog(
            isExpandedScreen = true,
            onClose = {},
        )
    }
}
*/
// endregion