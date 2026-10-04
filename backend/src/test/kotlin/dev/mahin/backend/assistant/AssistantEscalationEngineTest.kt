package dev.mahin.backend.assistant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AssistantEscalationEngineTest {
    private val fixtureChunk =
        dev.mahin.backend.assistant.AssistantFixtureCorpus.chunks
            .first()

    @Test
    fun crisisTermEscalates() {
        val result =
            AssistantEscalationEngine.evaluate(
                question = "فکر خودکشی دارم",
                retrievedChunks = listOf(fixtureChunk),
            )
        assertEquals(AssistantEscalationEngine.Decision.ESCALATE_CRISIS, result.decision)
    }

    @Test
    fun diagnosisRequestRefused() {
        val result =
            AssistantEscalationEngine.evaluate(
                question = "آیا باردارم؟",
                retrievedChunks = listOf(fixtureChunk),
            )
        assertEquals(AssistantEscalationEngine.Decision.REFUSE_DIAGNOSIS, result.decision)
    }

    @Test
    fun prescriptionRequestRefused() {
        val result =
            AssistantEscalationEngine.evaluate(
                question = "دوز این دارو چقدر است؟",
                retrievedChunks = listOf(fixtureChunk),
            )
        assertEquals(AssistantEscalationEngine.Decision.REFUSE_PRESCRIPTION, result.decision)
    }

    @Test
    fun noGroundingRefused() {
        val result =
            AssistantEscalationEngine.evaluate(
                question = "سوال تصادفی بدون منبع",
                retrievedChunks = emptyList(),
            )
        assertEquals(AssistantEscalationEngine.Decision.REFUSE_NO_GROUNDING, result.decision)
    }

    @Test
    fun groundedQuestionProceeds() {
        val result =
            AssistantEscalationEngine.evaluate(
                question = "m12-fixture-token آموزشی",
                retrievedChunks = listOf(fixtureChunk),
            )
        assertEquals(AssistantEscalationEngine.Decision.PROCEED, result.decision)
    }
}
