package dev.mahin.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mahin.features")
data class MahinFeatureFlagsProperties(
    /** Remote launch flag for optional Health Connect integration (default off). */
    val healthConnect: Boolean = false,
    /**
     * Remote kill switch for the health assistant (default off).
     * When false, assistant APIs fail closed and clients must hide assistant UI.
     */
    val healthAssistant: Boolean = false,
) {
    fun asPublicMap(): Map<String, Boolean> =
        mapOf(
            FEATURE_HEALTH_CONNECT to healthConnect,
            FEATURE_HEALTH_ASSISTANT to healthAssistant,
        )

    companion object {
        const val FEATURE_HEALTH_CONNECT = "health_connect"
        const val FEATURE_HEALTH_ASSISTANT = "health_assistant"
    }
}
