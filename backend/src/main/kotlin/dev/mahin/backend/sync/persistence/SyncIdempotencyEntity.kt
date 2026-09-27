package dev.mahin.backend.sync.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "sync_idempotency")
@IdClass(SyncIdempotencyEntity.Key::class)
class SyncIdempotencyEntity(
    @Id
    @Column(name = "owner_key", nullable = false, length = 80)
    var ownerKey: String,
    @Id
    @Column(name = "idempotency_key", nullable = false, length = 128)
    var idempotencyKey: String,
    @Column(name = "mutation_fingerprint", nullable = false, length = 64)
    var mutationFingerprint: String,
    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    var responseJson: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
) {
    data class Key(
        var ownerKey: String = "",
        var idempotencyKey: String = "",
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }
}
