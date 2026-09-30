package dev.mahin.core.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinCredentialStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
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

        fun hasPin(): Boolean = prefs.contains(HASH_KEY)

        fun setPin(pin: String) {
            val salt = PinHasher.generateSalt()
            val hash = PinHasher.hashPin(pin, salt)
            prefs
                .edit()
                .putString(SALT_KEY, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(HASH_KEY, hash)
                .commit()
        }

        fun clearPin() {
            prefs
                .edit()
                .remove(SALT_KEY)
                .remove(HASH_KEY)
                .commit()
        }

        fun verifyPin(pin: String): Boolean {
            val saltEncoded = prefs.getString(SALT_KEY, null) ?: return false
            val hash = prefs.getString(HASH_KEY, null) ?: return false
            val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
            return PinHasher.verifyPin(pin, salt, hash)
        }

        private companion object {
            const val PREFS_NAME = "mahin_pin_credential"
            const val SALT_KEY = "pin_salt_v1"
            const val HASH_KEY = "pin_hash_v1"
        }
    }
