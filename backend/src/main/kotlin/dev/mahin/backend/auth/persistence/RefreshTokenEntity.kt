package dev.mahin.backend.auth.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "auth_refresh_token")
@Suppress("LongParameterList")
class RefreshTokenEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "token_hash", nullable = false, unique = true, length = 64, columnDefinition = "VARCHAR(64)")
    var tokenHash: String,
    @Column(name = "user_id")
    var userId: UUID? = null,
    @Column(name = "guest_installation_id")
    var guestInstallationId: UUID? = null,
    @Column(name = "device_id", nullable = false)
    var deviceId: UUID,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
)
