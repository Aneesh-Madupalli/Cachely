package com.cachely.app.data

/**
 * Result of a cache cleaning operation. Simple by design; extend only after v1.
 */
data class CleaningResult(
    val totalBytesFreed: Long,
    val appsCleaned: Int,
    val appsSkipped: Int
)
