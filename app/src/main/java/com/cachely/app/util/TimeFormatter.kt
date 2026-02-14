package com.cachely.app.util

import java.util.concurrent.TimeUnit

/**
 * Formats timestamps as relative time (e.g. "2 hours ago").
 * No timers or background work.
 */
object TimeFormatter {
    fun formatRelative(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diff = nowMillis - timestampMillis
        return when {
            diff < 0 -> "just now"
            diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
                if (mins == 1L) "1 minute ago" else "$mins minutes ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                if (hours == 1L) "1 hour ago" else "$hours hours ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                if (days == 1L) "yesterday" else "$days days ago"
            }
            else -> "long ago"
        }
    }
}
