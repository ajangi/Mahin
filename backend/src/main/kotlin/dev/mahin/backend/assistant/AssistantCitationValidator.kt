package dev.mahin.backend.assistant

object AssistantCitationValidator {
    fun isValid(
        response: AssistantGatewayResponse,
        retrievedChunks: List<RetrievedContentChunk>,
    ): Boolean {
        if (response.citations.isEmpty()) {
            return false
        }
        val allowedPairs =
            retrievedChunks
                .map { Pair(it.documentId.toString(), it.versionId.toString()) }
                .toSet()
        return response.citations.all { citation ->
            Pair(citation.documentId, citation.versionId) in allowedPairs
        }
    }
}
