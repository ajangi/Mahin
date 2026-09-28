package dev.mahin.backend.entitlement

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class EntitlementConfiguration {
    /**
     * Local/test only. Production must use Google Play Developer API (deferred).
     */
    @Bean
    @Profile("local", "test", "dev")
    fun devGooglePlayPurchaseVerifier(): GooglePlayPurchaseVerifier = DevGooglePlayPurchaseVerifier()

    @Bean
    @Profile("!local & !test & !dev")
    fun productionGooglePlayPurchaseVerifier(): GooglePlayPurchaseVerifier = RejectingGooglePlayPurchaseVerifier()
}
