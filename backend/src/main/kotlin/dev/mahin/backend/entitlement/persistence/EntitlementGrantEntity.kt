package dev.mahin.backend.entitlement.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "entitlement_grant")
@Suppress("LongParameterList")
class EntitlementGrantEntity(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "tier", nullable = false) val tier: String,
    @Column(name = "source", nullable = false) val source: String,
    @Column(name = "starts_at", nullable = false) val startsAt: Instant,
    @Column(name = "expires_at") val expiresAt: Instant?,
    @Column(name = "created_at", nullable = false) val createdAt: Instant,
    @Column(name = "revoked_at") val revokedAt: Instant?,
)
