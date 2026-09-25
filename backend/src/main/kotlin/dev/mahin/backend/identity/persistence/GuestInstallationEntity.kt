package dev.mahin.backend.identity.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "guest_installation")
class GuestInstallationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "local_user_id", nullable = false, unique = true)
    var localUserId: UUID,
    @Column(name = "linked_user_id")
    var linkedUserId: UUID? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "converted_at")
    var convertedAt: Instant? = null,
)
