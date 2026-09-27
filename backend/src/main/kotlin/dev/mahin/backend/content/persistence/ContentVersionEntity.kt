package dev.mahin.backend.content.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "content_version")
@Suppress("LongParameterList")
class ContentVersionEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "document_id", nullable = false)
    var documentId: UUID = UUID.randomUUID(),
    @Column(name = "version_number", nullable = false)
    var versionNumber: Int = 1,
    @Column(name = "title", nullable = false, length = 500)
    var title: String = "",
    @Column(name = "search_index_text", length = 2500)
    var searchIndexText: String? = null,
    @Column(name = "summary", length = 2000)
    var summary: String? = null,
    @Column(name = "body_richtext", columnDefinition = "TEXT")
    var bodyRichtext: String? = null,
    @Column(name = "content_type", nullable = false, length = 64)
    var contentType: String = "article",
    @Column(name = "life_stage", length = 64)
    var lifeStage: String? = null,
    @Column(name = "gestational_week")
    var gestationalWeek: Int? = null,
    @Column(name = "tags_json", columnDefinition = "TEXT")
    var tagsJson: String? = "[]",
    @Column(name = "medical_risk_level", nullable = false, length = 32)
    var medicalRiskLevel: String = "none",
    @Column(name = "status", nullable = false, length = 32)
    var status: String = "draft",
    @Column(name = "review_stage", length = 32)
    var reviewStage: String? = null,
    @Column(name = "effective_from")
    var effectiveFrom: Instant? = null,
    @Column(name = "effective_to")
    var effectiveTo: Instant? = null,
    @Column(name = "source_publication_date")
    var sourcePublicationDate: LocalDate? = null,
    @Column(name = "source_last_checked_at")
    var sourceLastCheckedAt: Instant? = null,
    @Column(name = "clinical_reviewer", length = 200)
    var clinicalReviewer: String? = null,
    @Column(name = "clinical_reviewed_at")
    var clinicalReviewedAt: Instant? = null,
    @Column(name = "next_review_due_at")
    var nextReviewDueAt: Instant? = null,
    @Column(name = "created_by_staff_id")
    var createdByStaffId: UUID? = null,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
