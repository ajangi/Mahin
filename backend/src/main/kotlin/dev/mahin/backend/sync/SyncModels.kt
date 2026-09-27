package dev.mahin.backend.sync

import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class SyncMutationRequest(
    @field:NotEmpty val mutations: List<@Valid SyncMutationItem>,
)

data class SyncMutationItem(
    @field:NotBlank @field:Size(max = 64) val entityType: String,
    @field:NotNull val entityId: UUID,
    @field:NotNull val operation: SyncMutationOperation,
    val clientRevision: Long? = null,
    @field:NotNull val updatedAt: Instant,
    val payload: JsonNode? = null,
    @field:NotBlank @field:Size(min = 8, max = 128) val idempotencyKey: String,
)

data class SyncMutationResultItem(
    val entityType: String,
    val entityId: UUID,
    val status: String,
    val serverRevision: Long? = null,
    val conflictCode: String? = null,
)

data class SyncMutationResponse(
    val results: List<SyncMutationResultItem>,
    val latestServerRevision: Long,
)

data class SyncChangeItem(
    val entityType: String,
    val entityId: UUID,
    val serverRevision: Long,
    val clientRevision: Long?,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    val payload: JsonNode?,
)

data class SyncChangesResponse(
    val changes: List<SyncChangeItem>,
    val latestServerRevision: Long,
    val hasMore: Boolean,
)
