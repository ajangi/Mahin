package dev.mahin.backend.assistant

import dev.mahin.backend.assistant.persistence.AssistantInteractionLogEntity
import dev.mahin.backend.assistant.persistence.AssistantInteractionLogRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AssistantAuditLogger(
    private val interactionLogRepository: AssistantInteractionLogRepository,
) {
    private val logger = LoggerFactory.getLogger(AssistantAuditLogger::class.java)

    fun record(metadata: AssistantInteractionMetadata) {
        interactionLogRepository.save(
            AssistantInteractionLogEntity(
                userId = metadata.userId,
                promptTemplateId = metadata.promptTemplateId,
                providerId = metadata.providerId,
                modelVersion = metadata.modelVersion,
                outcomeClass = metadata.outcomeClass.name,
            ),
        )
        logger.info(
            "assistant_interaction template={} provider={} model={} outcome={}",
            metadata.promptTemplateId,
            metadata.providerId,
            metadata.modelVersion,
            metadata.outcomeClass.name,
        )
    }
}
