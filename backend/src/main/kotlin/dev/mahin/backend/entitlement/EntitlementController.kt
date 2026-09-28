package dev.mahin.backend.entitlement

import dev.mahin.backend.api.currentRegisteredUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class EntitlementController(
    private val entitlementService: EntitlementService,
) {
    @GetMapping("/entitlements/me")
    fun currentEntitlement(): EntitlementResponse {
        val userId = currentRegisteredUser().userId
        return entitlementService.currentEntitlement(userId)
    }

    @PostMapping("/billing/google-play/verify")
    fun verifyGooglePlayPurchase(
        @RequestBody body: GooglePlayVerifyRequest,
    ): GooglePlayBillingResponse {
        val userId = currentRegisteredUser().userId
        return entitlementService.verifyGooglePlayPurchase(userId, body.productId, body.purchaseToken)
    }

    @PostMapping("/billing/google-play/restore")
    fun restoreGooglePlayPurchases(
        @RequestBody body: GooglePlayRestoreRequest,
    ): GooglePlayBillingResponse {
        val userId = currentRegisteredUser().userId
        return entitlementService.restoreGooglePlayPurchases(userId, body.purchases)
    }
}
