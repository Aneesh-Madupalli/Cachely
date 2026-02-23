package com.cachely.app.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.data.AppCacheItem
import com.cachely.app.data.CleaningProgress
import com.cachely.app.data.CleaningResult
import com.cachely.app.ui.theme.CachelyTheme
import com.cachely.app.util.ByteFormatter

private const val ICON_SIZE_DP = 40

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val navigateToPermission by viewModel.navigateToPermission.collectAsState(initial = false)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.setAccessibilityGranted(AccessibilityHelper(context).isAccessibilityServiceEnabled())
    }
    LaunchedEffect(navigateToPermission) {
        if (navigateToPermission) {
            onNavigateToPermission()
            viewModel.clearNavigateToPermission()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        state = state,
        onCleanSelected = { viewModel.onCleanSelected(context) },
        onCancelCleaning = { viewModel.requestCancel() },
        onToggleSelection = { viewModel.toggleSelection(it) },
        onSelectAll = { viewModel.selectAll() },
        onClearSelection = { viewModel.clearSelection() },
        modifier = modifier
    )
}

@Composable
private fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            val d = context.packageManager.getApplicationIcon(packageName)
            val sizePx = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
            val b = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            d.setBounds(0, 0, sizePx, sizePx)
            d.draw(c)
            b
        } catch (_: Exception) {
            null
        }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.size(ICON_SIZE_DP.dp)
        )
    } else {
        Box(
            modifier = modifier
                .size(ICON_SIZE_DP.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppRow(
    item: AppCacheItem,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        shape = RoundedCornerShape(Design.radiusSmall),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Design.spaceStandard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.spaceInner)
        ) {
            AppIcon(packageName = item.packageName)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (item.approxCacheBytes > 0L) {
                        "Cache: ${ByteFormatter.format(item.approxCacheBytes)}"
                    } else {
                        "Cache: Ready to clean"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onCleanSelected: () -> Unit,
    onCancelCleaning: () -> Unit = {},
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val padding = screenPadding()
    val selectedReclaimableBytes = remember(state.appList, state.selectedPackageNames) {
        state.appList
            .filter { it.packageName in state.selectedPackageNames }
            .sumOf { it.approxCacheBytes }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Design.spaceSmall)
        ) {
            Text(
                text = "Cachely",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (state.isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Design.spaceLarge),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        } else if (state.appList.isEmpty()) {
            Text(
                text = "No apps to show",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Design.spaceLarge)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Design.spaceSmall)
            ) {
                items(
                    state.appList,
                    key = { it.packageName }
                ) { item ->
                    AppRow(
                        item = item,
                        isSelected = item.packageName in state.selectedPackageNames,
                        onToggleSelection = { onToggleSelection(item.packageName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Design.spaceInner))

        if (state.selectedPackageNames.isNotEmpty() && !state.isCleaning) {
            val n = state.selectedPackageNames.size
            val totalLabel = if (selectedReclaimableBytes > 0L) {
                " · ${ByteFormatter.format(selectedReclaimableBytes)} total cache"
            } else {
                ""
            }
            Text(
                text = "$n app${if (n == 1) "" else "s"} selected$totalLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Design.spaceSmall)
            )
            if (state.appList.isNotEmpty()) {
                val allSelected = n == state.appList.size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(Design.radiusSmall),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            if (allSelected) {
                                onClearSelection()
                            } else {
                                onSelectAll()
                            }
                        }
                    ) {
                        Text(
                            text = if (allSelected) "Clear selection" else "Select all apps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                horizontal = Design.spaceStandard,
                                vertical = Design.spaceMicro
                            )
                        )
                    }
                }
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale = animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = tween(durationMillis = 120)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCleanSelected,
                enabled = !state.isCleaning && state.selectedPackageNames.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                shape = RoundedCornerShape(Design.radiusSmall),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                interactionSource = interactionSource
            ) {
                Text(
                    text = when {
                        state.isCleaning -> "Cleaning…"
                        else -> "Clean selected apps"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (!state.isCleaning && state.selectedPackageNames.isNotEmpty()) {
                Text(
                    text = "You stay in control",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Design.spaceMicro)
                )
            }
        }

        val progress = state.progress
        if (state.isCleaning && progress != null) {
            Spacer(modifier = Modifier.height(Design.spaceSmall))
            ProgressChip(progress = progress)
            Text(
                text = "Currently assisting: ${progress.currentAppName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Design.spaceMicro)
            )
            Text(
                text = "Cancel after current app",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onCancelCleaning)
                    .padding(vertical = Design.spaceSmall)
            )
        }

        val result = state.result
        if (result != null) {
            Spacer(modifier = Modifier.height(Design.spaceSection))
            ResultSummary(
                result = result,
                lastCleaned = state.lastCleaned
            )
            Spacer(modifier = Modifier.height(Design.spaceSmall))
            Text(
                text = "Your apps are lighter — nothing else was touched",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Design.spaceMicro)
            )
        }

        Spacer(modifier = Modifier.height(Design.spacePage))
        Text(
            text = "No ads • No trackers",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ProgressChip(progress: CleaningProgress) {
    Surface(
        shape = RoundedCornerShape(Design.radiusSmall),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(Design.spaceStandard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.spaceInner)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Text(
                text = "Cleaning ${progress.currentIndex} of ${progress.totalApps}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ResultSummary(result: CleaningResult, lastCleaned: String?) {
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
                text = "Cleaning complete",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = buildResultLine(result),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (result.totalBytesFreed > 0L) {
                Text(
                    text = "Cache cleared: ${ByteFormatter.format(result.totalBytesFreed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                equivalentLine(result.totalBytesFreed)?.let { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            lastCleaned?.let { last ->
                Text(
                    text = "Last cleaned: $last",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildResultLine(result: CleaningResult): String {
    val parts = mutableListOf<String>()
    parts.add("${result.appsCleaned} app${if (result.appsCleaned == 1) "" else "s"} cleaned")
    if (result.appsSkipped > 0) {
        parts.add("${result.appsSkipped} skipped")
    }
    result.durationSeconds?.let { s ->
        parts.add("${s}s")
    }
    return parts.joinToString(" • ")
}

/** Optional "equivalent" line for psychological reward; only real numbers, never inflated. */
private fun equivalentLine(bytes: Long): String? {
    if (bytes <= 0L) return null
    val mb = bytes / (1024 * 1024)
    return when {
        mb >= 100 -> "That’s a lot of temporary data removed."
        mb >= 10 -> "Roughly equivalent to hundreds of cached thumbnails."
        mb >= 1 -> "Roughly equivalent to dozens of cached images."
        else -> null
    }
}

@Preview(name = "Home (idle)", showBackground = true)
@Composable
private fun HomeScreenPreviewIdle() {
    CachelyTheme {
        HomeScreenContent(
            state = HomeUiState(
                appList = listOf(
                    AppCacheItem("App One", "com.one", 1000L, false),
                    AppCacheItem("App Two", "com.two", 0L, false)
                ),
                accessibilityGranted = true
            ),
            onCleanSelected = {},
            onCancelCleaning = {},
            onToggleSelection = {},
            onSelectAll = {},
            onClearSelection = {}
        )
    }
}

@Preview(name = "Home (cleaning)", showBackground = true)
@Composable
private fun HomeScreenPreviewCleaning() {
    CachelyTheme {
        HomeScreenContent(
            state = HomeUiState(
                isCleaning = true,
                progress = CleaningProgress(12, 50, "WhatsApp"),
                accessibilityGranted = true
            ),
            onCleanSelected = {},
            onCancelCleaning = {},
            onToggleSelection = {},
            onSelectAll = {},
            onClearSelection = {}
        )
    }
}

@Preview(name = "Home (result)", showBackground = true)
@Composable
private fun HomeScreenPreviewResult() {
    CachelyTheme {
        HomeScreenContent(
            state = HomeUiState(
                lastCleaned = "2 min ago",
                result = CleaningResult(totalBytesFreed = 256_000_000L, appsCleaned = 12, appsSkipped = 2, durationSeconds = 45),
                accessibilityGranted = true
            ),
            onCleanSelected = {},
            onCancelCleaning = {},
            onToggleSelection = {},
            onSelectAll = {},
            onClearSelection = {}
        )
    }
}
