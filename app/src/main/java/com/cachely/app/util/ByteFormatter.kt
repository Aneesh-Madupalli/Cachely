package com.cachely.app.util

/**
 * Formats byte counts as KB / MB / GB. Locale-safe, lightweight.
 */
object ByteFormatter {
    fun format(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = -1
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        val unit = units[unitIndex]
        return when {
            value >= 100 -> "%.0f %s".format(value, unit)
            value >= 10 -> "%.1f %s".format(value, unit)
            value >= 1 -> "%.2f %s".format(value, unit)
            else -> "%.2f %s".format(value, unit)
        }
    }
}
