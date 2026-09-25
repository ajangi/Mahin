package dev.mahin.backend.sync.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sync_entity_record")
@Suppress("LongParameterList")
class SyncEntityRecordEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "owner_scope_key", nullable = false, length = 80)
    var ownerScopeKey: String,
    @Column(name = "guest_installation_id")
    var guestInstallationId: UUID? = null,
    @Column(name = "owner_user_id")
    var ownerUserId: UUID? = null,
    @Column(name = "entity_type", nullable = false, length = 64)
    var entityType: String,
    @Column(name = "entity_id", nullable = false)
    var entityId: UUID,
    @Column(name = "server_revision", nullable = false)
    var serverRevision: Long,
    @Column(name = "client_revision")
    var clientRevision: Long? = null,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    var payloadJson: String,
)
