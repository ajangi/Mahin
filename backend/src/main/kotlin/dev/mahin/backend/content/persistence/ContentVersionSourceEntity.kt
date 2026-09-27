package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "content_version_source")
@IdClass(ContentVersionSourceEntity.Pk::class)
class ContentVersionSourceEntity(
    @Id
    @Column(name = "version_id", nullable = false)
    var versionId: UUID = UUID.randomUUID(),
    @Id
    @Column(name = "source_id", nullable = false)
    var sourceId: UUID = UUID.randomUUID(),
) {
    data class Pk(
        val versionId: UUID = UUID.randomUUID(),
        val sourceId: UUID = UUID.randomUUID(),
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }
}
