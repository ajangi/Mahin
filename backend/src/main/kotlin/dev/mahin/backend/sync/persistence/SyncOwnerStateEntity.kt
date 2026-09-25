package dev.mahin.backend.sync.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sync_owner_state")
class SyncOwnerStateEntity(
    @Id
    @Column(name = "owner_key", nullable = false, length = 80)
    var ownerKey: String,
    @Column(name = "next_server_revision", nullable = false)
    var nextServerRevision: Long = 1,
)
