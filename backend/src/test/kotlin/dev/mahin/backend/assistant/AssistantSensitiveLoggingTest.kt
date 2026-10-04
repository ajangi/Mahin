package dev.mahin.backend.assistant

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import dev.mahin.backend.assistant.persistence.AssistantInteractionLogEntity
import dev.mahin.backend.assistant.persistence.AssistantInteractionLogRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.slf4j.LoggerFactory

class AssistantSensitiveLoggingTest {
    @Test
    fun auditLoggerDoesNotEmitIntimateQuestionText() {
        val repository = mock(AssistantInteractionLogRepository::class.java)
        `when`(repository.save(any(AssistantInteractionLogEntity::class.java))).thenAnswer { it.arguments[0] }
        val auditLogger = AssistantAuditLogger(repository)
        val logger = LoggerFactory.getLogger(AssistantAuditLogger::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        logger.addAppender(appender)

        val sensitiveQuestion = "یادداشت خصوصی: خونریزی شدید و درد لگن"
        auditLogger.record(
            AssistantInteractionMetadata(
                userId = UUID.randomUUID(),
                promptTemplateId = "template-v0",
                providerId = "fake",
                modelVersion = "fake-deterministic-v0",
                outcomeClass = AssistantOutcomeClass.ANSWERED,
            ),
        )

        val joined = appender.list.joinToString("\n") { it.formattedMessage }
        assertFalse(joined.contains(sensitiveQuestion))
        assertTrue(joined.contains("assistant_interaction"))
        assertTrue(joined.contains("template-v0"))
    }
}
