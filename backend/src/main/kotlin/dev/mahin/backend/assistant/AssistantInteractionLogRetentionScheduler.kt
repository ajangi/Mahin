package dev.mahin.backend.assistant

import dev.mahin.backend.assistant.persistence.AssistantInteractionLogRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AssistantInteractionLogRetentionScheduler(
    private val interactionLogRepository: AssistantInteractionLogRepository,
    @Value("\${mahin.assistant.log-retention-days:90}") private val retentionDays: Long,
) {
    private val logger = LoggerFactory.getLogger(AssistantInteractionLogRetentionScheduler::class.java)

    @Scheduled(fixedDelayString = "\${mahin.assistant.log-retention-delay-ms:86400000}")
    @Transactional
    fun purgeExpiredInteractionLogs() {
        val cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS)
        val deleted = interactionLogRepository.deleteAllCreatedBefore(cutoff)
        if (deleted > 0) {
            logger.info("assistant_interaction_log_retention purged_count={}", deleted)
        }
    }
}
