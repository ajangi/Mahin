package dev.mahin.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates and stores a SQLCipher passphrase in Keystore-backed EncryptedSharedPreferences.
 * Passphrase bytes are never logged.
 */
@Singleton
class KeystoreDeviceKeyMaterial
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : DeviceKeyMaterial {
        private val prefs by lazy {
            val masterKey =
                MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        override fun databasePassphrase(): ByteArray {
            val existing = prefs.getString(PASSPHRASE_KEY, null)
            if (existing != null) {
                return existing.toByteArray(Charsets.UTF_8)
            }
            val random = SecureRandom()
            val bytes = ByteArray(PASSPHRASE_BYTE_LENGTH)
            random.nextBytes(bytes)
            val encoded =
                bytes.joinToString(separator = "") { byte ->
                    "%02x".format(byte.toInt() and 0xff)
                }
            val committed =
                prefs
                    .edit()
                    .putString(PASSPHRASE_KEY, encoded)
                    .commit()
            check(committed) { "Failed to persist database passphrase" }
            return encoded.toByteArray(Charsets.UTF_8)
        }

        private companion object {
            const val PREFS_NAME = "mahin_db_key_material"
            const val PASSPHRASE_KEY = "sqlcipher_passphrase_v1"
            const val PASSPHRASE_BYTE_LENGTH = 32
        }
    }
