package com.cachely.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.accessibility.CleanCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PER_APP_TIMEOUT_MS = 15_000L
private const val MAX_APPS_TO_CLEAN = 100

/**
 * Single orchestration point for all cache cleaning operations.
 * Checks Accessibility availability, selects assisted or manual path,
 * coordinates cleaning, aggregates results. No UI, no node traversal,
 * no persistence, no logging.
 */
class CacheCleaner(private val context: Context) {

    private val accessibilityHelper = AccessibilityHelper(context)

    suspend fun cleanCache(): CleaningResult = withContext(Dispatchers.Main.immediate) {
        val enabled = accessibilityHelper.isAccessibilityServiceEnabled()
        if (enabled) {
            runAssistedClean()
        } else {
            runManualFlow()
        }
    }

    private suspend fun runAssistedClean(): CleaningResult = withContext(Dispatchers.Default) {
        val packages = getThirdPartyPackages().take(MAX_APPS_TO_CLEAN)
        var cleaned = 0
        var skipped = 0
        for (pkg in packages) {
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
        // We don't compute bytes freed without storage APIs; use 0 for v1
        CleaningResult(totalBytesFreed = 0L, appsCleaned = cleaned, appsSkipped = skipped)
    }

    private fun runManualFlow(): CleaningResult {
        val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) { }
        return CleaningResult(totalBytesFreed = 0L, appsCleaned = 0, appsSkipped = 0)
    }

    private fun getThirdPartyPackages(): List<String> {
        val pm = context.packageManager
        return pm.getInstalledApplications(0)
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { it.packageName }
            .distinct()
    }
}
