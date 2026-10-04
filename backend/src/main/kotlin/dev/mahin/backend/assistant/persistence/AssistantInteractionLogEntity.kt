package dev.mahin.backend.assistant.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "assistant_interaction_log")
@Suppress("LongParameterList")
class AssistantInteractionLogEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "prompt_template_id", nullable = false, length = 128)
    var promptTemplateId: String,
    @Column(name = "provider_id", nullable = false, length = 64)
    var providerId: String,
    @Column(name = "model_version", nullable = false, length = 64)
    var modelVersion: String,
    @Column(name = "outcome_class", nullable = false, length = 64)
    var outcomeClass: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
