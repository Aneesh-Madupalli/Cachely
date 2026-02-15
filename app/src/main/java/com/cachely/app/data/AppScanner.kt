package com.cachely.app.data

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.UserHandle
import android.os.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Fetches installed apps and builds app list with metadata.
 * Runs off main thread.
 *
 * Cache size (StorageStatsManager, API 26+):
 * - Requires PACKAGE_USAGE_STATS (Usage Access). User must grant in Settings;
 *   when not granted, all cache sizes are 0.
 * - On some OEMs / Android 11+ the system may still return 0 for privacy.
 * - We never persist or show "fake" sizes; 0 is shown as 0 B.
 *
 * Package visibility (Android 11+): Manifest must declare <queries> so
 * getInstalledApplications() returns more than a handful of apps.
 *
 * Sort: by approxCacheBytes descending, then appName, then packageName for stability.
 */
class AppScanner(private val context: Context) {

    suspend fun scan(
        excludeSystemApps: Boolean = true,
        excludeZeroCache: Boolean = false
    ): List<AppCacheItem> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val canReadCache = hasUsageAccess()
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { info ->
                val appName = info.loadLabel(pm)?.toString() ?: info.packageName
                val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val cacheBytes = if (canReadCache) getApproxCacheSize(info.packageName) else 0L
                AppCacheItem(
                    appName = appName,
                    packageName = info.packageName,
                    approxCacheBytes = cacheBytes,
                    isSystemApp = isSystem
                )
            }
            .filter { if (excludeSystemApps) !it.isSystemApp else true }
            // Only filter zero-cache when we can read cache; otherwise all are 0 and we'd remove everyone
            .filter { if (excludeZeroCache && canReadCache) it.approxCacheBytes > 0 else true }
            .sortedWith(
                compareByDescending<AppCacheItem> { it.approxCacheBytes }
                    .thenBy { it.appName }
                    .thenBy { it.packageName }
            )
        apps
    }

    /** True if app has Usage Access (required for StorageStatsManager for other packages). */
    fun hasUsageAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
            ?: return false
        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    /**
     * Approximate cache size via StorageStatsManager (API 26+).
     * Returns 0 when: no Usage Access, API < 26, or query throws (OEM/permission).
     * Production: user must grant Usage Access in Settings for non-zero values.
     */
    private fun getApproxCacheSize(packageName: String): Long {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return 0L
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE)
                as? StorageStatsManager ?: return 0L
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE)
                as? StorageManager ?: return 0L
            val volume = storageManager.getStorageVolume(Environment.getDataDirectory()) ?: return 0L
            val uuidStr = volume.uuid ?: return 0L
            val uuid = UUID.fromString(uuidStr)
            val userHandle = UserHandle.getUserHandleForUid(Process.myUid())
            val stats = storageStatsManager.queryStatsForPackage(uuid, packageName, userHandle)
            stats.cacheBytes
        } catch (_: Exception) {
            0L
        }
    }
}
