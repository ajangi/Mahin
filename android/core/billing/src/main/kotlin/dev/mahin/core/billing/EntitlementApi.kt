package dev.mahin.core.billing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class EntitlementApiResponse(
    val tier: String,
    @SerialName("expiresAt") val expiresAt: String? = null,
    val features: List<String> = emptyList(),
    @SerialName("verifiedAt") val verifiedAt: String,
)

@Serializable
data class GooglePlayVerifyRequest(
    val productId: String,
    val purchaseToken: String,
)

@Serializable
data class GooglePlayRestoreRequest(
    val purchases: List<GooglePlayVerifyRequest>,
)

@Serializable
data class GooglePlayBillingResponse(
    val tier: String,
    @SerialName("expiresAt") val expiresAt: String? = null,
)

interface EntitlementApi {
    @GET("v1/entitlements/me")
    suspend fun currentEntitlement(
        @Header("Authorization") authorization: String,
    ): EntitlementApiResponse

    @POST("v1/billing/google-play/verify")
    suspend fun verifyPurchase(
        @Header("Authorization") authorization: String,
        @Body body: GooglePlayVerifyRequest,
    ): GooglePlayBillingResponse

    @POST("v1/billing/google-play/restore")
    suspend fun restorePurchases(
        @Header("Authorization") authorization: String,
        @Body body: GooglePlayRestoreRequest,
    ): GooglePlayBillingResponse
}
