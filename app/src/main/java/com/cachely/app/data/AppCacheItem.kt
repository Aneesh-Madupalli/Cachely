package com.cachely.app.data

/**
 * Metadata for an installed app shown in the app list.
 * Icon is loaded in UI from PackageManager using packageName.
 */
data class AppCacheItem(
    val appName: String,
    val packageName: String,
    val approxCacheBytes: Long,
    val isSystemApp: Boolean
)
