package com.cachely.app.data

/**
 * Result of a cache cleaning operation. Simple by design; extend only after v1.
 * [totalBytesFreed] comes from extracted Storage screen values during assisted clean (0 if not readable).
 * [durationSeconds] is elapsed time for the run (null if not measured).
 */
data class CleaningResult(
    val totalBytesFreed: Long,
    val appsCleaned: Int,
    val appsSkipped: Int,
    val durationSeconds: Int? = null
)
