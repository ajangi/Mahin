package dev.mahin.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mahin.features")
data class MahinFeatureFlagsProperties(
    /** Remote launch flag for optional Health Connect integration (default off). */
    val healthConnect: Boolean = false,
) {
    fun asPublicMap(): Map<String, Boolean> =
        mapOf(
            FEATURE_HEALTH_CONNECT to healthConnect,
        )

    companion object {
        const val FEATURE_HEALTH_CONNECT = "health_connect"
    }
}
