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
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .padding(vertical = Design.spaceSmall)
                .clickable(onClick = onNotNow)
        ) {
            Text(
                text = "‹ Back",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = Design.spaceStandard,
                    vertical = Design.spaceMicro
                )
            )
        }
        Spacer(modifier = Modifier.height(Design.spaceSection))
        Text(
            text = "Assisted cleaning",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Design.spaceMicro))
        Text(
            text = "Only when you ask. Never in the background.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Design.spaceSection))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Design.radiusMedium),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(Design.spaceStandard),
                verticalArrangement = Arrangement.spacedBy(Design.spaceInner)
            ) {
                Text(
                    text = "What Cachely does",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Opens app storage screens\n• Taps \"Clear cache\" like a human\n• Stops instantly if access is revoked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "What Cachely never does",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• No background actions\n• No data or file deletion\n• No action without user intent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
            }
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
                "Enable assisted cleaning",
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
