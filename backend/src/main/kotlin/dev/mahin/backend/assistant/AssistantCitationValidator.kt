package dev.mahin.backend.assistant

object AssistantCitationValidator {
    fun isValid(
        response: AssistantGatewayResponse,
        retrievedChunks: List<RetrievedContentChunk>,
    ): Boolean {
        if (response.citations.isEmpty()) {
            return false
        }
        val documentIds = retrievedChunks.map { it.documentId.toString() }.toSet()
        val versionIds = retrievedChunks.map { it.versionId.toString() }.toSet()
        return response.citations.all { citation ->
            citation.documentId in documentIds && citation.versionId in versionIds
        }
    }
}
