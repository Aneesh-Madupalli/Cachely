package com.cachely.app.accessibility

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Session-based coordination between CacheCleaner (orchestrator) and CachelyAccessibilityService.
 *
 * Safety (Play policy + no rogue automation):
 * - Automation runs ONLY while an explicit session is active.
 * - Session is started when user taps "Clean Selected" and ended when the cleaning loop finishes
 *   (or on timeout). When app is closed or user didn't start a clean, no session → service does nothing.
 * - Session has a max lifetime so it cannot run indefinitely.
 */
object CleanCoordinator {

    private const val SESSION_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes

    private val clearedChannel = Channel<Unit>(1)
    private val sessionIdRef = AtomicReference<UUID?>(null)
    private val sessionStartedAtMs = AtomicLong(0L)

    /**
     * Start an automation session. Call only when user has just tapped "Clean Selected".
     * Drains any stale cleared signal from a previous run.
     */
    fun startSession() {
        sessionIdRef.set(UUID.randomUUID())
        sessionStartedAtMs.set(System.currentTimeMillis())
        while (clearedChannel.tryReceive().isSuccess) { /* drain stale */ }
    }

    /**
     * End the current session. Call when cleaning loop finishes (success, cancel, or error).
     * After this, the accessibility service must not perform automation until the next startSession().
     */
    fun endSession() {
        sessionIdRef.set(null)
        sessionStartedAtMs.set(0L)
    }

    /**
     * True only while a session is active and not expired.
     * AccessibilityService must check this before performing any automation.
     */
    fun isSessionActive(): Boolean {
        val id = sessionIdRef.get() ?: return false
        val started = sessionStartedAtMs.get()
        if (started == 0L) return false
        if (System.currentTimeMillis() - started > SESSION_MAX_AGE_MS) {
            endSession()
            return false
        }
        return true
    }

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
