package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "content_document")
@Suppress("LongParameterList")
class ContentDocumentEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "slug", nullable = false, length = 200)
    var slug: String = "",
    @Column(name = "locale", nullable = false, length = 16)
    var locale: String = "fa-IR",
    @Column(name = "published_version_id")
    var publishedVersionId: UUID? = null,
    @Column(name = "withdrawn_at")
    var withdrawnAt: Instant? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
