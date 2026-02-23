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

/**
 * Explains why Usage Access is needed (to show app cache sizes) and opens system settings.
 * Same pattern as [PermissionScreen] for Assisted cleaning.
 */
@Composable
fun UsageAccessScreen(
    onOpenSettings: () -> Unit,
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
            text = "Usage Access",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Design.spaceMicro))
        Text(
            text = "So you can see how much cache each app is using.",
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
                    text = "Why Cachely needs this",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Usage Access lets the system tell Cachely the approximate cache size per app. With it, you see values like \"~124 MB reclaimable\" on the home screen. Without it, sizes show as \"Ready to clean\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Cachely uses this only to display sizes. It does not collect or send usage data. You can revoke access anytime in device settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
            }
        }
        Spacer(modifier = Modifier.height(Design.spacePage))
        Button(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                } catch (_: Exception) { }
                onOpenSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(Design.radiusSmall),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                "Open settings",
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

@Preview(name = "Usage Access", showBackground = true)
@Composable
private fun UsageAccessScreenPreview() {
    CachelyTheme {
        UsageAccessScreen(
            onOpenSettings = {},
            onNotNow = {}
        )
    }
}
