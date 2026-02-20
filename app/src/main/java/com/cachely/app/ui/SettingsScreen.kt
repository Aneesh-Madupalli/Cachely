package com.cachely.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.cachely.app.ui.theme.CachelyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPermission: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assistedPreferred by viewModel.assistedPreferred.collectAsState(initial = false)
    SettingsScreenContent(
        assistedPreferred = assistedPreferred,
        onAssistedPreferredChange = { viewModel.setAssistedPreferred(it) },
        onNavigateToPermission = onNavigateToPermission,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    assistedPreferred: Boolean,
    onAssistedPreferredChange: (Boolean) -> Unit,
    onNavigateToPermission: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val padding = screenPadding()
    val versionName = remember {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName ?: "1.0"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            }
        }.getOrElse { "1.0" }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("←", style = MaterialTheme.typography.titleMedium)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(Design.spaceLarge)
        ) {
            // App Information
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Design.radiusMedium),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Design.spaceStandard),
                    verticalArrangement = Arrangement.spacedBy(Design.spaceSmall)
                ) {
                    Text(
                        text = "App Information",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Version",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = versionName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Cachely — minimal cache cleaner. One action, no ads, no trackers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Assisted cleaning
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Design.radiusMedium),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Design.spaceStandard)
                ) {
                    Text(
                        text = "Assisted cleaning",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Design.spaceSmall))
                    Text(
                        text = "Allows Cachely to guide cache clearing screens. You can turn this off anytime in device settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Design.spaceInner))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assisted cleaning",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = assistedPreferred,
                            onCheckedChange = onAssistedPreferredChange
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Design.spaceSmall),
                        color = MaterialTheme.colorScheme.surface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPermission() }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configure access",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Transparency (cache size estimation + usage access)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Design.radiusMedium),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Design.spaceStandard),
                    verticalArrangement = Arrangement.spacedBy(Design.spaceSmall)
                ) {
                    Text(
                        text = "Transparency",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Cache size is an estimate from the system. To show approximate size per app, grant Usage Access; otherwise sizes show as \"Ready to clean\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Accessibility is used only to tap \"Clear cache\" when you start a clean. No screen reading, no data storage, no background use.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grant Usage Access",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Support & Legal
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Design.radiusMedium),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Design.spaceStandard),
                    verticalArrangement = Arrangement.spacedBy(Design.spaceSmall)
                ) {
                    Text(
                        text = "Support & Legal",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
                                try { context.startActivity(intent) } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/terms"))
                                try { context.startActivity(intent) } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terms & Conditions",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                try { context.startActivity(intent) } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rate app",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${context.packageName}")
                                }
                                try { context.startActivity(Intent.createChooser(intent, null)) } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Share app",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@example.com")
                                }
                                try { context.startActivity(intent) } catch (_: Exception) { }
                            }
                            .padding(vertical = Design.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contact support",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Settings", showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CachelyTheme {
        SettingsScreenContent(
            assistedPreferred = true,
            onAssistedPreferredChange = {},
            onNavigateToPermission = {},
            onNavigateBack = {}
        )
    }
}
