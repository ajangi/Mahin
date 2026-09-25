package dev.mahin.backend.identity

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class GuestBootstrapRequest(
    @field:NotNull val localUserId: UUID,
    @field:NotBlank @field:Size(max = 32) val platform: String,
    val appVersion: String? = null,
)

data class GuestBootstrapResponse(
    val guestInstallationId: UUID,
    val localUserId: UUID,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val deviceId: UUID,
)

data class ConvertGuestRequest(
    @field:NotNull val localUserId: UUID,
    val guestAccessToken: String? = null,
    val guestRefreshToken: String? = null,
)

data class ConvertGuestResponse(
    val userId: UUID,
    val guestInstallationId: UUID,
    val migratedEntityCount: Int,
)
