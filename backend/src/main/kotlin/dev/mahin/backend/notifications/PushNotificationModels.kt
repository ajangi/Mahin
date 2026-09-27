package dev.mahin.backend.notifications

import java.time.Instant
import java.util.UUID

data class RegisterPushTokenRequest(
    val provider: String,
    val token: String,
)

data class PushTokenRegistrationResponse(
    val deviceId: UUID,
    val provider: String,
    val registeredAt: Instant,
)
