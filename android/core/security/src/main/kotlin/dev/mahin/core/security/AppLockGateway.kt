package dev.mahin.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Keystore-backed secret material. M9 completes lock/encryption product flows;
 * this boundary exists so M2 persistence does not invent an ad-hoc key store.
 */
interface DeviceKeyMaterial {
    fun databasePassphrase(): ByteArray
}

data class AppLockSettings(
    val enabled: Boolean,
    val hideRecentsPreview: Boolean,
    val blockScreenshots: Boolean,
)

interface AppLockGateway {
    val isLockEnabled: Boolean

    val sessionRevision: StateFlow<Int>

    fun arePreferencesLoaded(): Boolean

    fun shouldShowLockGate(): Boolean

    fun lockSettings(): AppLockSettings

    fun isSessionUnlocked(): Boolean

    fun markSessionUnlocked()

    fun lockSession()

    fun shouldHideRecentsPreview(): Boolean

    fun shouldBlockScreenshots(): Boolean

    fun requiresUnlockForSensitiveAction(): Boolean = shouldShowLockGate()
}

object DisabledAppLockGateway : AppLockGateway {
    override val isLockEnabled: Boolean = false

    override val sessionRevision: StateFlow<Int> = MutableStateFlow(0)

    override fun arePreferencesLoaded(): Boolean = true

    override fun shouldShowLockGate(): Boolean = false

    override fun lockSettings(): AppLockSettings =
        AppLockSettings(
            enabled = false,
            hideRecentsPreview = false,
            blockScreenshots = false,
        )

    override fun isSessionUnlocked(): Boolean = true

    override fun markSessionUnlocked() = Unit

    override fun lockSession() = Unit

    override fun shouldHideRecentsPreview(): Boolean = false

    override fun shouldBlockScreenshots(): Boolean = false
}
