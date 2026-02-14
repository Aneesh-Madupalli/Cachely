package com.cachely.app.accessibility

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Coordinates between CacheCleaner (orchestrator) and CachelyAccessibilityService.
 * Service calls notifyCleared() when it has tapped "Clear cache" and navigated back.
 * CacheCleaner awaits awaitCleared(timeoutMs) between opening each App Info screen.
 */
object CleanCoordinator {

    private val clearedChannel = Channel<Unit>(1)

    /** Call from AccessibilityService when "Clear cache" was tapped and we've navigated back. */
    fun notifyCleared() {
        clearedChannel.trySend(Unit)
    }

    /** Suspend until cleared or timeout. Returns true if cleared, false if timeout. */
    suspend fun awaitCleared(timeoutMs: Long): Boolean {
        return withTimeoutOrNull(timeoutMs) {
            clearedChannel.receive()
            true
        } ?: false
    }
}
