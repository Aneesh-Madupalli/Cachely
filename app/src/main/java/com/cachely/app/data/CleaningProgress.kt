package com.cachely.app.data

/**
 * Progress during sequential cache cleaning.
 * Displayed as e.g. "Cleaning 12 / 50 — WhatsApp"
 */
data class CleaningProgress(
    val currentIndex: Int,
    val totalApps: Int,
    val currentAppName: String
)
