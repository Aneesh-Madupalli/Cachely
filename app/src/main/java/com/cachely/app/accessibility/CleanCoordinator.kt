package com.cachely.app.accessibility

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Session-based coordination between CacheCleaner (orchestrator) and CachelyAccessibilityService.
 *
 * State machine: automation is gated by [CleaningPhase]. EXPECT_STORAGE = we're on App Info,
 * only click "Storage". EXPECT_CLEAR_CACHE = we're on Storage screen, only click "Clear cache".
 * Prevents acting on the wrong screen and getting stuck after the first app.
 *
 * Safety (Play policy + no rogue automation):
 * - Automation runs ONLY while an explicit session is active.
 * - Session is started when user taps "Clean Selected" and ended when the cleaning loop finishes.
 * - Session has a max lifetime so it cannot run indefinitely.
 */
object CleanCoordinator {

    enum class CleaningPhase {
        /** On App Info; only click "Storage" / "Storage & cache". */
        EXPECT_STORAGE,
        /** On Storage screen; only click "Clear cache". */
        EXPECT_CLEAR_CACHE
    }

    private const val SESSION_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes

    private val clearedChannel = Channel<Unit>(1)
    private val sessionIdRef = AtomicReference<UUID?>(null)
    private val sessionStartedAtMs = AtomicLong(0L)
    private val phaseRef = AtomicReference(CleaningPhase.EXPECT_STORAGE)
    /** After notifyCleared we see "back to App Info" once; skip one Storage click so we don't reopen same app. */
    private val skipStorageClicksRef = AtomicInteger(0)

    /**
     * Start an automation session. Call only when user has just tapped "Clean Selected".
     * Resets phase to EXPECT_STORAGE and drains any stale cleared signal.
     */
    fun startSession() {
        sessionIdRef.set(UUID.randomUUID())
        sessionStartedAtMs.set(System.currentTimeMillis())
        phaseRef.set(CleaningPhase.EXPECT_STORAGE)
        skipStorageClicksRef.set(0)
        while (clearedChannel.tryReceive().isSuccess) { /* drain stale */ }
    }

    /**
     * End the current session. Call when cleaning loop finishes (success, cancel, or error).
     * After this, the accessibility service must not perform automation until the next startSession().
     */
    fun endSession() {
        sessionIdRef.set(null)
        sessionStartedAtMs.set(0L)
        phaseRef.set(CleaningPhase.EXPECT_STORAGE)
        skipStorageClicksRef.set(0)
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

    /** Current phase for state-gated automation. */
    fun getPhase(): CleaningPhase = phaseRef.get()

    /** Call from AccessibilityService when it has clicked "Storage" and we're now on Storage screen. */
    fun setPhaseExpectClearCache() {
        phaseRef.set(CleaningPhase.EXPECT_CLEAR_CACHE)
    }

    /**
     * Call from AccessibilityService when "Clear cache" was tapped and we've navigated back.
     * Resets phase to EXPECT_STORAGE for the next app and marks that the next Storage screen
     * (back to previous app's App Info) must be skipped so we don't double-act.
     */
    fun notifyCleared() {
        phaseRef.set(CleaningPhase.EXPECT_STORAGE)
        skipStorageClicksRef.set(1)
        clearedChannel.trySend(Unit)
    }

    /**
     * If we should skip the next "Storage" click (we're on back-to-App-Info of the app we just cleared), consume and return true.
     * Otherwise return false.
     */
    fun consumeSkipStorageClick(): Boolean {
        return skipStorageClicksRef.getAndUpdate { if (it > 0) it - 1 else 0 } > 0
    }

    /** Suspend until cleared or timeout. Returns true if cleared, false if timeout. */
    suspend fun awaitCleared(timeoutMs: Long): Boolean {
        return withTimeoutOrNull(timeoutMs) {
            clearedChannel.receive()
            true
        } ?: false
    }
}
