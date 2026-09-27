package dev.mahin.backend.entitlement

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EntitlementConfiguration {
    @Bean
    fun googlePlayPurchaseVerifier(): GooglePlayPurchaseVerifier = DevGooglePlayPurchaseVerifier()
}
