package com.cachely.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.accessibility.CleanCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PER_APP_TIMEOUT_MS = 7_000L

/**
 * Single orchestration point for all cache cleaning operations.
 * Cleans only selected packages. Reports progress per app. Sequential only.
 * No UI, no node traversal, no persistence, no logging.
 */
class CacheCleaner(private val context: Context) {

    private val accessibilityHelper = AccessibilityHelper(context)

    /**
     * @param selectedPackages Package names to clean (user-selected).
     * @param appNameResolver Returns display name for a package (for progress).
     * @param onProgress Called before each app; pass null to ignore.
     * @param isCancelled Checked between apps; when true, stop after current app.
     */
    suspend fun cleanCache(
        selectedPackages: List<String>,
        appNameResolver: (String) -> String,
        onProgress: (suspend (CleaningProgress) -> Unit)? = null,
        isCancelled: () -> Boolean = { false }
    ): CleaningResult = withContext(Dispatchers.Main.immediate) {
        val enabled = accessibilityHelper.isAccessibilityServiceEnabled()
        if (enabled && selectedPackages.isNotEmpty()) {
            runAssistedClean(selectedPackages, appNameResolver, onProgress, isCancelled)
        } else if (selectedPackages.isEmpty()) {
            CleaningResult(totalBytesFreed = 0L, appsCleaned = 0, appsSkipped = 0)
        } else {
            runManualFlow(selectedPackages)
        }
    }

    private suspend fun runAssistedClean(
        packages: List<String>,
        appNameResolver: (String) -> String,
        onProgress: (suspend (CleaningProgress) -> Unit)?,
        isCancelled: () -> Boolean
    ): CleaningResult = withContext(Dispatchers.Default) {
        CleanCoordinator.startSession()
        try {
            var cleaned = 0
            var skipped = 0
            val total = packages.size
            for ((index, pkg) in packages.withIndex()) {
                if (isCancelled()) break
                if (!CleanCoordinator.isSessionActive()) break
                onProgress?.invoke(
                    CleaningProgress(
                        currentIndex = index + 1,
                        totalApps = total,
                        currentAppName = appNameResolver(pkg)
                    )
                )
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkg")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    withContext(Dispatchers.Main.immediate) { context.startActivity(intent) }
                    val cleared = CleanCoordinator.awaitCleared(PER_APP_TIMEOUT_MS)
                    if (cleared) cleaned++ else skipped++
                } catch (_: Exception) {
                    skipped++
                }
            }
            CleaningResult(totalBytesFreed = 0L, appsCleaned = cleaned, appsSkipped = skipped)
        } finally {
            CleanCoordinator.endSession()
        }
    }

    private fun runManualFlow(selectedPackages: List<String>): CleaningResult {
        val first = selectedPackages.firstOrNull() ?: return CleaningResult(0L, 0, 0)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$first")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) { }
        return CleaningResult(totalBytesFreed = 0L, appsCleaned = 0, appsSkipped = 0)
    }

    fun isAccessibilityEnabled(): Boolean = accessibilityHelper.isAccessibilityServiceEnabled()
}
