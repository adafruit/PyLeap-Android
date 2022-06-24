package com.adafruit.pyleap.ui.about

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.R
import com.adafruit.pyleap.ui.dialog.CustomDialog
import com.adafruit.pyleap.ui.theme.PyLeapTheme

@Composable
fun AboutDialog(
    isExpandedScreen: Boolean,
    onClose: () -> Unit,
) {
    CustomDialog(
        title = "About",
        isExpandedScreen = isExpandedScreen,
        onClose = onClose,
    ) {
        AboutContents(isExpandedScreen = isExpandedScreen)
    }
}

@Composable
private fun AboutContents(
    isExpandedScreen: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val uriHandler = LocalUriHandler.current

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = Modifier.padding(bottom = 20.dp),
                painter = painterResource(id = R.drawable.info_adafruit_logo),
                contentDescription = null, // decorative element
            )
        }

        Text(
            "PyLeap is designed for use with specific devices using the CircuitPython BLE FileTransfer service.\n" + "\n" + "Follow the links below to purchase a compatible device from the Adafruit shop:\n"
        )

        Column(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Circuit Playground Bluefruit
            ClickableLink(
                prefix = "· ",
                title = "Circuit Playground Bluefruit",
                link = "https://www.adafruit.com/product/4333"
            )

            // Adafruit CLUE
            ClickableLink(
                prefix = "· ",
                title = "Adafruit CLUE",
                link = "https://www.adafruit.com/product/4500"
            )
        }

        ClickableLink(
            prefix = "Before you can use this app, you'll need to update your uf2 firmware file onto your device. Learn more in the ",
            title = "PyLeap Learn Guide",
            link = "https://learn.adafruit.com/pyleap-app"
        )

    }
}

@Composable
private fun ClickableLink(prefix: String, title: String, link: String) {
    val annotatedString = buildAnnotatedString {
        append(prefix)

        pushStringAnnotation(tag = "link", annotation = link)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(title)
        }
        pop()
    }

    val uriHandler = LocalUriHandler.current
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}

// region Previews
@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    PyLeapTheme {
        AboutContents(
            isExpandedScreen = false,
            //onClose = {},
        )
    }
}
// endregion