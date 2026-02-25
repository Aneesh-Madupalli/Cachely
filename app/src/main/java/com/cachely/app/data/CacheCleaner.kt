package com.cachely.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.accessibility.CleanCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val PER_APP_TIMEOUT_MS = 5_000L
/** Delay between finishing one app and opening the next so back animation and state settle. */
private const val BETWEEN_APPS_DELAY_MS = 400L
private const val MAX_RETRIES_PER_APP = 0

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
            CleaningResult(totalBytesFreed = 0L, appsCleaned = 0, appsSkipped = 0, durationSeconds = null)
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
        val startTimeMs = System.currentTimeMillis()
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
                var cleared = false
                var attempts = 0
                while (attempts <= MAX_RETRIES_PER_APP && !cleared) {
                    try {
                        withContext(Dispatchers.Main.immediate) { context.startActivity(intent) }
                        cleared = CleanCoordinator.awaitCleared(PER_APP_TIMEOUT_MS)
                    } catch (_: Exception) { }
                    if (!cleared && attempts < MAX_RETRIES_PER_APP) {
                        delay(BETWEEN_APPS_DELAY_MS)
                        attempts++
                    } else break
                }
                if (cleared) cleaned++ else skipped++
                if (index < packages.size - 1) delay(BETWEEN_APPS_DELAY_MS)
            }
            if (cleaned + skipped == packages.size) {
                finishCleaningSession() // return to Cachely, end automation
            }
            val durationSeconds = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
            CleaningResult(
                totalBytesFreed = CleanCoordinator.getTotalBytesCleared(),
                appsCleaned = cleaned,
                appsSkipped = skipped,
                durationSeconds = durationSeconds
            )
        } finally {
            CleanCoordinator.endSession()
        }
    }

    /**
     * Called when all selected apps have been processed. Brings Cachely to front and ends the
     * automation session so the service stops acting and state resets to IDLE.
     * Must be called from a coroutine (uses Main for startActivity).
     */
    private suspend fun finishCleaningSession() {
        CleanCoordinator.endSession()
        withContext(Dispatchers.Main.immediate) {
            try {
                val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?: return@withContext
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(launch)
            } catch (_: Exception) { }
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
        return CleaningResult(totalBytesFreed = 0L, appsCleaned = 0, appsSkipped = 0, durationSeconds = null)
    }

    fun isAccessibilityEnabled(): Boolean = accessibilityHelper.isAccessibilityServiceEnabled()
}
