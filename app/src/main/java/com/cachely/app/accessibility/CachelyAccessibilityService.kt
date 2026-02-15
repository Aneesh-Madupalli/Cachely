package com.cachely.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Two-phase automation on Settings (App Info) screens:
 * 1) If we see "Storage" / "Storage & cache" → click it to open storage screen.
 * 2) If we see "Clear cache" → click it, wait, back, then notify cleared.
 *
 * Guard: Automation runs ONLY when CleanCoordinator.isSessionActive() is true (session
 * started by user tapping "Clean Selected"). When app is closed or user didn't start a clean,
 * no session → we do nothing. Prevents rogue automation when user manually opens App Info.
 *
 * Does not read or store screen content. User-initiated only. Compliant with Play policy.
 */
class CachelyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastNotifyAtMs = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != "com.android.settings") return
        if (!CleanCoordinator.isSessionActive()) return
        if (System.currentTimeMillis() - lastNotifyAtMs < NOTIFY_COOLDOWN_MS) return
        val root = event.source ?: rootInActiveWindow ?: return
        try {
            // Phase 2: We're on Storage screen — find and click "Clear cache", then back + notify
            if (findAndClickClearCache(root)) {
                handler.postDelayed({
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    lastNotifyAtMs = System.currentTimeMillis()
                    CleanCoordinator.notifyCleared()
                }, CLEAR_CACHE_SETTLE_MS)
                return
            }
            // Phase 1: We're on App Info main — find and click "Storage" to open storage screen
            findAndClickStorage(root)
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {}

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
        /** Ignore events for this long after notifyCleared() to avoid double-acting on same transition. */
        private const val NOTIFY_COOLDOWN_MS = 1500L
    }
}
