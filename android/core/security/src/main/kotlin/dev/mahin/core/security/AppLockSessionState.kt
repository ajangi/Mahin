package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesSnapshot

/** In-memory lock session; kept pure for unit tests and used by [DefaultAppLockGateway]. */
data class AppLockSessionState(
    val preferencesLoaded: Boolean = false,
    val snapshot: AppLockPreferencesSnapshot = AppLockPreferencesSnapshot(),
    val sessionUnlocked: Boolean = false,
) {
    fun shouldShowGate(): Boolean =
        AppLockGateEvaluator.shouldShowGate(
            preferencesLoaded = preferencesLoaded,
            mode = snapshot.mode,
            sessionUnlocked = effectiveSessionUnlocked(),
        )

    fun shouldShowLoading(): Boolean = AppLockGateEvaluator.shouldShowLoading(preferencesLoaded)

    fun isLockEnabled(): Boolean =
        if (!preferencesLoaded) {
            true
        } else {
            snapshot.mode != AppLockMode.DISABLED
        }

    fun effectiveSessionUnlocked(): Boolean {
        if (!preferencesLoaded) return false
        if (snapshot.mode == AppLockMode.DISABLED) return true
        return sessionUnlocked
    }

    fun onPreferencesLoaded(newSnapshot: AppLockPreferencesSnapshot): AppLockSessionState {
        val wasDisabled = snapshot.mode == AppLockMode.DISABLED
        val unlocked =
            when {
                newSnapshot.mode == AppLockMode.DISABLED -> true
                wasDisabled -> false
                else -> sessionUnlocked
            }
        return copy(preferencesLoaded = true, snapshot = newSnapshot, sessionUnlocked = unlocked)
    }

    fun markUnlocked(): AppLockSessionState = copy(sessionUnlocked = true)

    fun lockSession(): AppLockSessionState =
        if (!preferencesLoaded || snapshot.mode == AppLockMode.DISABLED) {
            copy(sessionUnlocked = false)
        } else {
            copy(sessionUnlocked = false)
        }
}
