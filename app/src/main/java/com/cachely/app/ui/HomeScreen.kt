package com.cachely.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.data.CleaningResult
import com.cachely.app.ui.theme.CachelyTheme
import com.cachely.app.util.ByteFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.setAssistedEnabled(AccessibilityHelper(context).isAccessibilityServiceEnabled())
    }
    HomeScreenContent(
        state = state,
        onNavigateToSettings = onNavigateToSettings,
        onClean = { viewModel.startCleaning() },
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onNavigateToSettings: () -> Unit,
    onClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    val padding = screenPadding()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Design.spaceSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cachely",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (state.assistedEnabled) "Assisted: ON" else "Assisted: OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Design.spaceMicro))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = Design.spaceStandard)
                        .padding(Design.spaceSmall)
                        .clickable { onNavigateToSettings() }
                )
            }
        }
        Spacer(modifier = Modifier.height(Design.spaceLarge))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Design.radiusMedium),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Design.spaceLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Design.spaceInner)
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale = animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    animationSpec = tween(durationMillis = 120)
                )
                Button(
                    onClick = onClean,
                    enabled = !state.isCleaning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                    shape = RoundedCornerShape(Design.radiusSmall),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    interactionSource = interactionSource
                ) {
                    Text(
                        text = if (state.isCleaning) "Cleaning…" else "Clean cache",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (state.isCleaning) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(Design.spaceSmall),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
        if (state.lastCleaned != null || state.result != null) {
            Spacer(modifier = Modifier.height(Design.spaceSection))
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
                    state.lastCleaned?.let { last ->
                        Text(
                            text = "Last cleaned: $last",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    state.result?.let { result ->
                        Text(
                            text = "Freed ${ByteFormatter.format(result.totalBytesFreed)} • ${result.appsCleaned} apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(Design.spacePage))
        Text(
            text = "No ads • No trackers",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Preview(name = "Home (idle)", showBackground = true)
@Composable
private fun HomeScreenPreviewIdle() {
    CachelyTheme {
        HomeScreenContent(
            state = HomeUiState(assistedEnabled = true),
            onNavigateToSettings = {},
            onClean = {}
        )
    }
}

@Preview(name = "Home (cleaning)", showBackground = true)
@Composable
private fun HomeScreenPreviewCleaning() {
    CachelyTheme {
        HomeScreenContent(
            state = HomeUiState(isCleaning = true, assistedEnabled = true),
            onNavigateToSettings = {},
            onClean = {}
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
                assistedEnabled = true,
                result = CleaningResult(totalBytesFreed = 256_000_000L, appsCleaned = 12, appsSkipped = 0)
            ),
            onNavigateToSettings = {},
            onClean = {}
        )
    }
}

