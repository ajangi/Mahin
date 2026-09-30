package dev.mahin.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class SecurityAuditService(
    private val repository: SecurityAuditEventRepository,
    private val objectMapper: ObjectMapper,
) {
    data class AuditRecord(
        val actorType: String,
        val actorId: String,
        val action: String,
        val targetType: String? = null,
        val targetId: String? = null,
        val metadata: Map<String, String> = emptyMap(),
    )

    fun record(entry: AuditRecord) {
        val metadataJson =
            if (entry.metadata.isEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(entry.metadata)
            }
        repository.save(
            SecurityAuditEventEntity(
                id = UUID.randomUUID(),
                actorType = entry.actorType,
                actorId = entry.actorId,
                action = entry.action,
                targetType = entry.targetType,
                targetId = entry.targetId,
                metadataJson = metadataJson,
                createdAt = Instant.now(),
            ),
        )
    }
}
