package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "content_source")
@Suppress("LongParameterList")
class ContentSourceEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "citation_key", nullable = false, unique = true, length = 64)
    var citationKey: String = "",
    @Column(name = "title", nullable = false, length = 500)
    var title: String = "",
    @Column(name = "url", length = 2000)
    var url: String? = null,
    @Column(name = "publication_date")
    var publicationDate: LocalDate? = null,
    @Column(name = "last_checked_at")
    var lastCheckedAt: Instant? = null,
    @Column(name = "notes", length = 2000)
    var notes: String? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
