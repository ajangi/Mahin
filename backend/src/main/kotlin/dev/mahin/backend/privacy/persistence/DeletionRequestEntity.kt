package dev.mahin.backend.privacy.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "deletion_request")
@Suppress("LongParameterList")
class DeletionRequestEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "status", nullable = false, length = 32)
    var status: String,
    @Column(name = "requested_at", nullable = false)
    var requestedAt: Instant,
    @Column(name = "scheduled_at")
    var scheduledAt: Instant? = null,
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
    @Column(name = "failure_reason", length = 256)
    var failureReason: String? = null,
)
