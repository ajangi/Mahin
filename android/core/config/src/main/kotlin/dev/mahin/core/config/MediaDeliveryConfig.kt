package dev.mahin.core.config

/**
 * Runtime environment. Production CDN hosts belong in remote/env config, never feature UI.
 */
enum class AppEnvironment {
    LOCAL,
    CI,
    STAGING,
    PRODUCTION,
}

data class MediaDeliveryConfig(
    val publicBaseUrl: String,
    val environment: AppEnvironment,
) {
    init {
        require(publicBaseUrl.isNotBlank()) { "Media public base URL is required" }
        require(!publicBaseUrl.contains("userId", ignoreCase = true)) {
            "Media base URL must not encode user identifiers"
        }
        if (environment == AppEnvironment.PRODUCTION) {
            require(publicBaseUrl.startsWith("https://")) { "Production media must be HTTPS" }
        }
    }

    fun urlFor(storageKey: String): String {
        require(storageKey.isNotBlank()) { "storageKey is required" }
        require(!storageKey.contains("://")) { "storageKey must not be an absolute URL" }
        require(!storageKey.contains("?")) { "storageKey must not contain query parameters" }
        val base = publicBaseUrl.trimEnd('/')
        val key = storageKey.trimStart('/')
        return "$base/$key"
    }
}

interface FeatureFlagGateway {
    fun isEnabled(flag: String): Boolean
}

class LocalFeatureFlagGateway : FeatureFlagGateway {
    override fun isEnabled(flag: String): Boolean = false
}
