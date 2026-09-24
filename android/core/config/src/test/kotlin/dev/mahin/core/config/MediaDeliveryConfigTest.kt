package dev.mahin.core.config

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MediaDeliveryConfigTest {
    @Test
    fun buildsUrlFromConfiguredBaseAndStorageKey() {
        val config =
            MediaDeliveryConfig(
                publicBaseUrl = "http://localhost:9000/mahin-media",
                environment = AppEnvironment.LOCAL,
            )
        assertThat(config.urlFor("placeholders/non-medical/foundation-mark/v1"))
            .isEqualTo("http://localhost:9000/mahin-media/placeholders/non-medical/foundation-mark/v1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAbsoluteUrlsAsStorageKeys() {
        MediaDeliveryConfig(
            publicBaseUrl = "http://localhost:9000/mahin-media",
            environment = AppEnvironment.LOCAL,
        ).urlFor("https://cdn.example/secret")
    }

    @Test(expected = IllegalArgumentException::class)
    fun productionRequiresHttps() {
        MediaDeliveryConfig(
            publicBaseUrl = "http://cdn.example/media",
            environment = AppEnvironment.PRODUCTION,
        )
    }
}
