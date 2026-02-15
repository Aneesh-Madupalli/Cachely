package com.cachely.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * State-gated automation on Settings (App Info) screens:
 * - EXPECT_STORAGE: on App Info; only click "Storage" / "Storage & cache" (skip first Storage after notify to avoid same app).
 * - EXPECT_CLEAR_CACHE: on Storage screen; only click "Clear cache", then back + notify.
 *
 * Guard: Automation runs ONLY when CleanCoordinator.isSessionActive() is true (session
 * started by user tapping "Clean Selected"). Prevents rogue automation when user manually opens App Info.
 *
 * Does not read or store screen content. User-initiated only. Compliant with Play policy.
 */
class CachelyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != "com.android.settings") return
        if (!CleanCoordinator.isSessionActive()) return
        val root = event.source ?: rootInActiveWindow ?: return
        try {
            when (CleanCoordinator.getPhase()) {
                CleanCoordinator.CleaningPhase.EXPECT_CLEAR_CACHE -> {
                    val bytesCleared = extractCacheSizeFromStorageScreen(root)
                    if (findAndClickClearCache(root)) {
                        handler.postDelayed({
                            performGlobalAction(GLOBAL_ACTION_BACK)
                            CleanCoordinator.notifyCleared(bytesCleared)
                        }, CLEAR_CACHE_SETTLE_MS)
                    }
                    return
                }
                CleanCoordinator.CleaningPhase.EXPECT_STORAGE -> {
                    if (CleanCoordinator.consumeSkipStorageClick()) return
                    if (findAndClickStorage(root)) {
                        CleanCoordinator.setPhaseExpectClearCache()
                    }
                }
            }
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {}

    /**
     * Extract cache size from Storage screen (e.g. "Cache" label with "124 MB").
     * Used only during user-initiated cleaning; returns 0 if not found or unreadable.
     */
    private fun extractCacheSizeFromStorageScreen(root: AccessibilityNodeInfo): Long {
        return try {
            extractCacheSizeRecursive(root)
        } catch (_: Exception) {
            0L
        }
    }

    private fun extractCacheSizeRecursive(node: AccessibilityNodeInfo): Long {
        val text = node.text?.toString()?.trim() ?: node.contentDescription?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            val bytes = parseSizeToBytes(text)
            if (bytes > 0L) return bytes
            if (text.equals("Cache", ignoreCase = true)) {
                val parent = node.parent ?: return 0L
                for (i in 0 until parent.childCount) {
                    val child = parent.getChild(i) ?: continue
                    try {
                        val childText = child.text?.toString()?.trim() ?: continue
                        val b = parseSizeToBytes(childText)
                        if (b > 0L) return b
                    } finally {
                        child.recycle()
                    }
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                val b = extractCacheSizeRecursive(child)
                if (b > 0L) return b
            } finally {
                child.recycle()
            }
        }
        return 0L
    }

    /** Parse strings like "124 MB", "1.5 GB", "512 KB" to bytes. Returns 0 if not matched. */
    private fun parseSizeToBytes(sizeText: String): Long {
        val cleaned = sizeText.replace(",", ".").trim()
        val regex = Regex("""([\d.]+)\s*(KB|MB|GB|[KMG])\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(cleaned) ?: return 0L
        val value = match.groupValues[1].toDoubleOrNull() ?: return 0L
        val unit = match.groupValues[2].uppercase()
        return when {
            unit.startsWith("K") -> (value * 1024).toLong()
            unit.startsWith("M") -> (value * 1024 * 1024).toLong()
            unit.startsWith("G") -> (value * 1024 * 1024 * 1024).toLong()
            else -> 0L
        }
    }

    /** Click "Clear cache" only (not "Clear data"). Returns true if clicked. */
    private fun findAndClickClearCache(root: AccessibilityNodeInfo): Boolean {
        val texts = listOf(
            "Clear cache",
            "Clear Cache",
            "clear cache"
        )
        for (text in texts) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                try {
                    val toClick = findClickable(node) ?: continue
                    val clicked = toClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    toClick.recycle()
                    if (clicked) return true
                } catch (_: Exception) { }
                node.recycle()
            }
        }
        // Fallback: recursive search (handles nested/scrollable content)
        return clickFirstMatchingRecursive(root) { n ->
            val t = n.text?.toString()?.trim() ?: return@clickFirstMatchingRecursive false
            texts.any { t.equals(it, ignoreCase = true) }
        }
    }

    /** Click "Storage" or "Storage & cache" to open storage screen. */
    private fun findAndClickStorage(root: AccessibilityNodeInfo): Boolean {
        val texts = listOf(
            "Storage",
            "Storage & cache",
            "Storage and cache",
            "Storage &amp; cache"
        )
        for (text in texts) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                try {
                    val toClick = findClickable(node) ?: continue
                    val clicked = toClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    toClick.recycle()
                    if (clicked) return true
                } catch (_: Exception) { }
                node.recycle()
            }
        }
        return clickFirstMatchingRecursive(root) { n ->
            val t = n.text?.toString()?.trim() ?: return@clickFirstMatchingRecursive false
            texts.any { t.equals(it, ignoreCase = true) || t.contains("storage", ignoreCase = true) && t.contains("cache", ignoreCase = true) }
        }
    }

    private fun findClickable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isClickable) return AccessibilityNodeInfo.obtain(node)
        val parent = node.parent ?: return null
        return if (parent.isClickable) AccessibilityNodeInfo.obtain(parent) else {
            parent.recycle()
            null
        }
    }

    /**
     * Recursive search: find first node matching predicate and click its clickable self/parent.
     * Recycles visited nodes; stops on first successful click.
     */
    private fun clickFirstMatchingRecursive(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): Boolean {
        if (predicate(node)) {
            val toClick = findClickable(node)
            return try {
                toClick?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
            } finally {
                toClick?.recycle()
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                if (clickFirstMatchingRecursive(child, predicate)) return true
            } finally {
                child.recycle()
            }
        }
        return false
    }

    companion object {
        /** Delay after clicking "Clear cache" before going back (let UI update). */
        private const val CLEAR_CACHE_SETTLE_MS = 450L
    }
}
