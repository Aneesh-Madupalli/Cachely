package com.cachely.app.accessibility

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Check if Accessibility service is enabled and open system Accessibility settings.
 * Does NOT perform UI automation, access node trees, or cleaning logic.
 */
class AccessibilityHelper(private val context: Context) {

    fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val enabled = am.getEnabledAccessibilityServiceList(
            android.view.accessibility.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ) ?: return false
        val packageName = context.packageName
        val serviceName = CachelyAccessibilityService::class.java.name
        return enabled.any { info ->
            info.resolveInfo?.serviceInfo?.let { s ->
                s.packageName == packageName && s.name == serviceName
            } ?: false
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
