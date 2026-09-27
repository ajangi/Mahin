package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_content_bookmark")
@IdClass(UserContentBookmarkEntity.Pk::class)
class UserContentBookmarkEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),
    @Id
    @Column(name = "document_id", nullable = false)
    var documentId: UUID = UUID.randomUUID(),
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
) {
    data class Pk(
        val userId: UUID = UUID.randomUUID(),
        val documentId: UUID = UUID.randomUUID(),
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }
}
