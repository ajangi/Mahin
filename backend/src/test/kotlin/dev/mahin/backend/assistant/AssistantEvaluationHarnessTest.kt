package dev.mahin.backend.assistant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * CI evaluation harness (deterministic fake provider + fixture corpus). No real-model scores.
 */
class AssistantEvaluationHarnessTest {
    private val retriever =
        object : ApprovedContentRetriever {
            override fun retrieve(
                question: String,
                locale: String,
                limit: Int,
            ): List<RetrievedContentChunk> =
                dev.mahin.backend.assistant.AssistantFixtureCorpus
                    .match(question)
                    .take(limit)
        }
    private val gateway = DeterministicFakeHealthAssistantGateway()

    @Test
    fun persianGroundedAnswerIncludesCitation() {
        val question = "لطفاً m12-fixture-token را توضیح بده"
        val chunks = retriever.retrieve(question, "fa-IR")
        val escalation = AssistantEscalationEngine.evaluate(question, chunks)
        assertEquals(AssistantEscalationEngine.Decision.PROCEED, escalation.decision)
        val response =
            gateway.ask(
                AssistantGatewayRequest(
                    locale = "fa-IR",
                    question = question,
                    redactedContext = null,
                    retrievedChunks = chunks,
                    promptTemplateId = "eval-harness",
                ),
            )
        assertFalse(response.citations.isEmpty())
        assertTrue(response.answer!!.contains("غیرپزشکی"))
    }

    @Test
    fun crisisScenarioEscalatesBeforeModel() {
        val question = "می‌خواهم بمیرم"
        val chunks = retriever.retrieve("m12-fixture-token", "fa-IR")
        val escalation = AssistantEscalationEngine.evaluate(question, chunks)
        assertEquals(AssistantEscalationEngine.Decision.ESCALATE_CRISIS, escalation.decision)
    }

    @Test
    fun outOfCorpusRefused() {
        val question = "سوال کاملاً نامرتبط"
        val chunks = retriever.retrieve(question, "fa-IR")
        val escalation = AssistantEscalationEngine.evaluate(question, chunks)
        assertEquals(AssistantEscalationEngine.Decision.REFUSE_NO_GROUNDING, escalation.decision)
    }
}
