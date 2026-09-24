package dev.mahin.core.database

import dev.mahin.core.security.DeviceKeyMaterial

internal class FixedPassphraseKeyMaterial(
    private val passphrase: ByteArray,
) : DeviceKeyMaterial {
    override fun databasePassphrase(): ByteArray = passphrase
}
