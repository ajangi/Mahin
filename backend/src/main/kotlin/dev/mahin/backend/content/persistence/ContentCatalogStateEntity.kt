package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "content_catalog_state")
class ContentCatalogStateEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: Int = 1,
    @Column(name = "publication_revision", nullable = false)
    var publicationRevision: Long = 0,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
