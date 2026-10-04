package dev.mahin.backend.assistant

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AssistantCitationValidatorTest {
    private val chunkA =
        RetrievedContentChunk(
            documentId = UUID.fromString("00000000-0000-4000-8000-00000000a12a"),
            versionId = UUID.fromString("00000000-0000-4000-8000-00000000a12b"),
            slug = "a",
            title = "A",
            excerpt = "excerpt",
            locale = "fa-IR",
        )
    private val chunkB =
        RetrievedContentChunk(
            documentId = UUID.fromString("00000000-0000-4000-8000-00000000b12a"),
            versionId = UUID.fromString("00000000-0000-4000-8000-00000000b12b"),
            slug = "b",
            title = "B",
            excerpt = "excerpt",
            locale = "fa-IR",
        )

    @Test
    fun validCitationMustMatchRetrievedDocumentVersionPair() {
        val response =
            AssistantGatewayResponse(
                answer = "x",
                citations =
                    listOf(
                        ContentCitation(
                            documentId = chunkA.documentId.toString(),
                            versionId = chunkA.versionId.toString(),
                            title = "A",
                            slug = "a",
                        ),
                    ),
                modelVersion = "stub",
                providerId = "stub",
            )
        assertTrue(AssistantCitationValidator.isValid(response, listOf(chunkA, chunkB)))
    }

    @Test
    fun mixedDocumentIdAndVersionIdFromDifferentChunksIsInvalid() {
        val response =
            AssistantGatewayResponse(
                answer = "x",
                citations =
                    listOf(
                        ContentCitation(
                            documentId = chunkA.documentId.toString(),
                            versionId = chunkB.versionId.toString(),
                            title = "x",
                            slug = "x",
                        ),
                    ),
                modelVersion = "stub",
                providerId = "stub",
            )
        assertFalse(AssistantCitationValidator.isValid(response, listOf(chunkA, chunkB)))
    }
}
