package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "content_audit_event")
@Suppress("LongParameterList")
class ContentAuditEventEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "actor_staff_id")
    val actorStaffId: UUID? = null,
    @Column(name = "actor_email", length = 320)
    val actorEmail: String? = null,
    @Column(name = "action", nullable = false, length = 64)
    val action: String = "",
    @Column(name = "document_id")
    val documentId: UUID? = null,
    @Column(name = "version_id")
    val versionId: UUID? = null,
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    val metadataJson: String? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
