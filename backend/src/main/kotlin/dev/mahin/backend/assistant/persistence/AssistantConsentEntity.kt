package dev.mahin.backend.assistant.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "assistant_consent")
class AssistantConsentEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "share_cycle_summary", nullable = false)
    var shareCycleSummary: Boolean = false,
    @Column(name = "share_symptom_tags", nullable = false)
    var shareSymptomTags: Boolean = false,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
