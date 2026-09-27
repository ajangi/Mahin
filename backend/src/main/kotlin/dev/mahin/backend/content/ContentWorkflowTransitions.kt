package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentWorkflowTransitions {
    @Suppress("CyclomaticComplexMethod")
    fun requireLegal(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        action: ContentWorkflowAction,
    ) {
        val legal =
            when (action) {
                ContentWorkflowAction.SUBMIT_MEDICAL_REVIEW -> version.status == "draft"
                ContentWorkflowAction.APPROVE_MEDICAL ->
                    version.status == "review" && version.reviewStage == ReviewStage.MEDICAL.name.lowercase()
                ContentWorkflowAction.SUBMIT_EDITORIAL_REVIEW ->
                    version.status == "review" &&
                        version.reviewStage == ReviewStage.MEDICAL.name.lowercase() &&
                        version.clinicalReviewedAt != null
                ContentWorkflowAction.APPROVE_EDITORIAL ->
                    version.status == "review" &&
                        (
                            version.reviewStage == ReviewStage.EDITORIAL.name.lowercase() ||
                                (
                                    version.reviewStage == ReviewStage.MEDICAL.name.lowercase() &&
                                        version.clinicalReviewedAt != null
                                )
                        )
                ContentWorkflowAction.PUBLISH -> version.status == "approved"
                ContentWorkflowAction.RETIRE ->
                    version.status == "published" &&
                        document.publishedVersionId == version.id &&
                        document.withdrawnAt == null
            }
        if (!legal) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "illegal_workflow_transition")
        }
    }
}
