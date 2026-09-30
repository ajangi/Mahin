package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockMode

/** Pure gate visibility rules shared by [DefaultAppLockGateway] and tests. */
object AppLockGateEvaluator {
    fun shouldShowLoading(preferencesLoaded: Boolean): Boolean = !preferencesLoaded

    fun shouldShowGate(
        preferencesLoaded: Boolean,
        mode: AppLockMode,
        sessionUnlocked: Boolean,
    ): Boolean =
        !preferencesLoaded ||
            (mode != AppLockMode.DISABLED && !sessionUnlocked)
}
