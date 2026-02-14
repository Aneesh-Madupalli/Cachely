package com.cachely.app.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cachely.app.ui.theme.CachelyTheme

@Composable
fun PermissionScreen(
    onEnable: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val padding = screenPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(Design.spaceSection))
        Text(
            text = "Assisted cleaning",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Design.spaceInner))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Design.radiusMedium),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                modifier = Modifier.padding(Design.spaceStandard),
                text = "Cachely can use Accessibility only to tap the \"Clear cache\" button on app info screens when you choose to clean. It does not read screen content, collect data, or run in the background. You can turn it off anytime in your device settings.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
        }
        Spacer(modifier = Modifier.height(Design.spacePage))
        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
                onEnable()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(Design.radiusSmall),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                "Enable assisted mode",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(Design.spaceInner))
        OutlinedButton(
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Design.radiusSmall)
        ) {
            Text(
                "Not now",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(name = "Permission", showBackground = true)
@Composable
private fun PermissionScreenPreview() {
    CachelyTheme {
        PermissionScreen(
            onEnable = {},
            onNotNow = {}
        )
    }
}
