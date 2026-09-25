package dev.mahin.backend.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 12, max = 128) val password: String,
    @field:NotBlank @field:Size(max = 32) val platform: String,
    val appVersion: String? = null,
    val localUserId: java.util.UUID? = null,
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank @field:Size(max = 32) val platform: String,
    val appVersion: String? = null,
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val userId: UUID? = null,
    val guestInstallationId: UUID? = null,
    val deviceId: UUID,
)

data class LogoutRequest(
    @field:NotBlank val refreshToken: String,
)
