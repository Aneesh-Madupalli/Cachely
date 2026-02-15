package com.cachely.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fetches installed apps and builds app list with metadata.
 * Runs off main thread. Cache size is approximate (0 when not available).
 */
class AppScanner(private val context: Context) {

    suspend fun scan(
        excludeSystemApps: Boolean = true,
        excludeZeroCache: Boolean = false
    ): List<AppCacheItem> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { info ->
                val appName = info.loadLabel(pm)?.toString() ?: info.packageName
                val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val cacheBytes = getApproxCacheSize(pm, info.packageName)
                AppCacheItem(
                    appName = appName,
                    packageName = info.packageName,
                    approxCacheBytes = cacheBytes,
                    isSystemApp = isSystem
                )
            }
            .filter { if (excludeSystemApps) !it.isSystemApp else true }
            .filter { if (excludeZeroCache) it.approxCacheBytes > 0 else true }
            .sortedWith(compareByDescending<AppCacheItem> { it.approxCacheBytes }.thenBy { it.appName })
        apps
    }

    /** Approximate cache size; returns 0 when not available (StorageStatsManager requires PACKAGE_USAGE_STATS for other apps). */
    private fun getApproxCacheSize(pm: PackageManager, packageName: String): Long {
        return 0L
    }
}
