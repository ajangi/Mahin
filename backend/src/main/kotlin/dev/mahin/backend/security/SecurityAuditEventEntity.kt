package dev.mahin.backend.security

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "security_audit_event")
@Suppress("LongParameterList")
class SecurityAuditEventEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "actor_type", nullable = false, length = 32)
    var actorType: String,
    @Column(name = "actor_id", nullable = false, length = 80)
    var actorId: String,
    @Column(name = "action", nullable = false, length = 64)
    var action: String,
    @Column(name = "target_type", length = 64)
    var targetType: String? = null,
    @Column(name = "target_id", length = 80)
    var targetId: String? = null,
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    var metadataJson: String? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
