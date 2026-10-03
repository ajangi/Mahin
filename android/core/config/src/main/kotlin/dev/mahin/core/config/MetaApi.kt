package dev.mahin.core.config

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class MetaApiResponse(
    val apiVersion: String,
    val environment: String,
    val product: String = "mahin",
    val featureFlags: Map<String, Boolean> = emptyMap(),
)

interface MetaApi {
    @GET("v1/meta")
    suspend fun meta(): MetaApiResponse
}
