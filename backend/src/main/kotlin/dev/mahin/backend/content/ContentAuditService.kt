package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.content.persistence.ContentAuditEventEntity
import dev.mahin.backend.content.persistence.ContentAuditEventRepository
import dev.mahin.backend.security.MahinAuthSubject
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class ContentAuditService(
    private val auditRepository: ContentAuditEventRepository,
    private val objectMapper: ObjectMapper,
) {
    fun record(
        actor: MahinAuthSubject.CmsStaff?,
        action: String,
        documentId: UUID? = null,
        versionId: UUID? = null,
        metadata: Map<String, String> = emptyMap(),
    ) {
        val metadataJson =
            if (metadata.isEmpty()) {
                null
            } else {
                objectMapper.writeValueAsString(metadata)
            }
        auditRepository.save(
            ContentAuditEventEntity(
                actorStaffId = actor?.staffId,
                actorEmail = actor?.email,
                action = action,
                documentId = documentId,
                versionId = versionId,
                metadataJson = metadataJson,
            ),
        )
    }
}
