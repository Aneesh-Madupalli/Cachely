package com.cachely.app.data

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.storage.StorageManager
import android.Manifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        excludeZeroCache: Boolean = true
    ): List<AppCacheItem> = withContext(Dispatchers.Default) {
        val pm: PackageManager = context.packageManager
        val rawApps: List<ApplicationInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val apps = rawApps
            .map { info ->
                val appName: String = info.loadLabel(pm).toString()
                val isSystemApp: Boolean = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isLaunchable: Boolean = pm.getLaunchIntentForPackage(info.packageName) != null
                val isEnabled: Boolean = info.enabled
                val cacheBytes: Long = getApproxCacheSize(info.packageName)
                val isCleanable: Boolean =
                    !isSystemApp &&
                        isLaunchable &&
                        isEnabled &&
                        cacheBytes > 0L

                AppCacheItem(
                    appName = appName,
                    packageName = info.packageName,
                    approxCacheBytes = cacheBytes,
                    isSystemApp = isSystemApp,
                    isLaunchable = isLaunchable,
                    isEnabled = isEnabled,
                    isCleanable = isCleanable
                )
            }
            // App is eligible for cache cleaning ONLY if it is launchable and enabled.
            .filter { it.isLaunchable && it.isEnabled }
            .filter { if (excludeSystemApps) !it.isSystemApp else true }
            .filter { if (excludeZeroCache) it.approxCacheBytes > 0L else true }
            // Order by cache size (largest first), then name, then package for stability.
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
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        if (mode == AppOpsManager.MODE_ALLOWED) return true
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                PackageManager.PERMISSION_GRANTED
        }
        return false
    }


    /**
     * Approximate cache size via StorageStatsManager (API 26+).
     * Returns 0 when: no Usage Access, API < 26, or query throws (OEM/permission).
     * Uses StorageManager.UUID_DEFAULT for primary internal storage so we don't depend on
     * getStorageVolume() or volume.uuid (which can be null or missing on some devices).
     */
    private fun getApproxCacheSize(packageName: String): Long {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return 0L
        return try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE)
                as? StorageStatsManager ?: return 0L
            val userHandle = UserHandle.getUserHandleForUid(Process.myUid())
            val stats = storageStatsManager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                packageName,
                userHandle
            )
            stats.cacheBytes
        } catch (_: Exception) {
            0L
        }
    }
}
