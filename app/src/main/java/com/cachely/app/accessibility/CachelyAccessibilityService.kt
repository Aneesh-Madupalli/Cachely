package com.cachely.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Observes window changes, detects App Info screen, locates "Clear cache" button,
 * executes click, navigates back. No screen reading, content extraction, storage, or background.
 * Per-app timeout, retry limit, abort on user cancellation.
 */
class CachelyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        // Only act on Settings (App Info) screens
        if (event.packageName?.toString() != "com.android.settings") return
        val source = event.source ?: rootInActiveWindow ?: return
        try {
            if (findAndClickClearCache(source)) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                CleanCoordinator.notifyCleared()
            }
        } finally {
            source.recycle()
        }
    }

    override fun onInterrupt() {}

    /**
     * Find a node with "Clear cache" (or common variants) and click it or its clickable parent.
     */
    private fun findAndClickClearCache(root: AccessibilityNodeInfo): Boolean {
        val candidates = listOf(
            "Clear cache",
            "Clear Cache",
            "clear cache",
            "Clear data",
            "Clear Data"
        )
        for (text in candidates) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                try {
                    val toClick = findClickable(node)
                    if (toClick?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                        toClick.recycle()
                        return true
                    }
                    toClick?.recycle()
                } catch (_: Exception) { }
                node.recycle()
            }
        }
        return false
    }

    private fun findClickable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isClickable) return AccessibilityNodeInfo.obtain(node)
        val parent = node.parent ?: return null
        return if (parent.isClickable) parent else {
            parent.recycle()
            null
        }
    }
}
