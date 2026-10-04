package dev.mahin.backend.assistant

import dev.mahin.backend.assistant.persistence.AssistantInteractionLogEntity
import dev.mahin.backend.assistant.persistence.AssistantInteractionLogRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AssistantInteractionLogRetentionIntegrationTest(
    @Autowired val scheduler: AssistantInteractionLogRetentionScheduler,
    @Autowired val repository: AssistantInteractionLogRepository,
) {
    @Test
    fun purgeDeletesRowsOlderThanRetentionWindow() {
        val userId = UUID.randomUUID()
        repository.save(
            AssistantInteractionLogEntity(
                userId = userId,
                promptTemplateId = "t",
                providerId = "fake",
                modelVersion = "v0",
                outcomeClass = "ANSWERED",
                createdAt = Instant.now().minus(120, ChronoUnit.DAYS),
            ),
        )
        assertEquals(1, repository.countByUserId(userId))
        scheduler.purgeExpiredInteractionLogs()
        assertEquals(0, repository.countByUserId(userId))
    }
}
