package dev.mahin.backend.device.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "device_installation")
@Suppress("LongParameterList")
class DeviceInstallationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "guest_installation_id")
    var guestInstallationId: UUID? = null,
    @Column(name = "owner_user_id")
    var ownerUserId: UUID? = null,
    @Column(name = "platform", nullable = false, length = 32)
    var platform: String,
    @Column(name = "app_version", length = 32)
    var appVersion: String? = null,
    @Column(name = "registered_at", nullable = false)
    var registeredAt: Instant,
    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: Instant,
)
