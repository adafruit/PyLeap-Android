package com.adafruit.pyleap.ui.connection

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adafruit.pyleap.ui.theme.ConnectionStatusError
import com.adafruit.pyleap.ui.theme.PyLeapTheme

@Composable
fun ConnectionCard(
    onOpenScanDialog: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(ConnectionStatusError)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            "Not connected to a device.",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        TextButton(onClick = onOpenScanDialog) {
            Text(
                "Connect now",
                style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
                color = Color.White
            )
        }
    }
}


// region Preview
@Preview(showBackground = true)
@Composable
fun ConnectionCardPreview() {

    PyLeapTheme {
        ConnectionCard(onOpenScanDialog = {})
    }
}
// endregion