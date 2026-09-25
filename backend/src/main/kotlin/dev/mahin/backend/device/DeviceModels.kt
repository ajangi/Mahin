package dev.mahin.backend.device

import java.time.Instant
import java.util.UUID

data class DeviceSessionResponse(
    val deviceId: UUID,
    val platform: String,
    val appVersion: String?,
    val registeredAt: Instant,
    val lastSeenAt: Instant,
)

data class DeviceListResponse(
    val devices: List<DeviceSessionResponse>,
)
