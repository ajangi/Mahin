package dev.mahin.core.security

/**
 * Keystore-backed secret material. M9 completes lock/encryption product flows;
 * this boundary exists so M2 persistence does not invent an ad-hoc key store.
 */
interface DeviceKeyMaterial {
    fun databasePassphrase(): ByteArray
}

interface AppLockGateway {
    val isLockEnabled: Boolean

    fun shouldHideRecentsPreview(): Boolean
}

object DisabledAppLockGateway : AppLockGateway {
    override val isLockEnabled: Boolean = false

    override fun shouldHideRecentsPreview(): Boolean = false
}
