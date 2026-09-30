package dev.mahin.backend.privacy

import java.time.Instant
import java.util.UUID

data class DeletionRequestResponse(
    val id: UUID,
    val status: String,
    val requestedAt: Instant,
    val scheduledAt: Instant?,
    val completedAt: Instant?,
)

data class ExportJobResponse(
    val id: UUID,
    val status: String,
    val requestedAt: Instant,
    val completedAt: Instant?,
)
