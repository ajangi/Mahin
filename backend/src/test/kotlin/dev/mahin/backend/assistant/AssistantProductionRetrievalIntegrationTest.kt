package dev.mahin.backend.assistant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
@SpringBootTest(properties = ["spring.profiles.active=prod"])
class AssistantProductionRetrievalIntegrationTest(
    @Autowired val retriever: ApprovedContentRetriever,
) {
    @Test
    fun outOfCorpusDoesNotReturnFixtureChunks() {
        val chunks = retriever.retrieve("سوال کاملاً نامرتبط بدون fixture", "fa-IR")
        assertTrue(chunks.isEmpty())
        assertTrue(chunks.none { it.slug == "fixture-m12-non-medical" })
    }

    @Test
    fun fixtureTokenDoesNotMatchInProd() {
        val chunks = retriever.retrieve("m12-fixture-token", "fa-IR")
        assertEquals(0, chunks.size)
    }
}
