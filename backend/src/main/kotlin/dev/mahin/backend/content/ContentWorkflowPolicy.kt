package dev.mahin.backend.content

import dev.mahin.backend.cms.CmsRole

enum class ContentWorkflowAction {
    SUBMIT_MEDICAL_REVIEW,
    APPROVE_MEDICAL,
    SUBMIT_EDITORIAL_REVIEW,
    APPROVE_EDITORIAL,
    PUBLISH,
    RETIRE,
}

object ContentWorkflowPolicy {
    fun canPerform(
        roles: Set<CmsRole>,
        action: ContentWorkflowAction,
    ): Boolean {
        if (roles.contains(CmsRole.SUPER_ADMIN)) {
            return true
        }
        return when (action) {
            ContentWorkflowAction.SUBMIT_MEDICAL_REVIEW,
            ContentWorkflowAction.SUBMIT_EDITORIAL_REVIEW,
            ->
                roles.contains(CmsRole.EDITOR)
            ContentWorkflowAction.APPROVE_MEDICAL ->
                roles.contains(CmsRole.MEDICAL_REVIEWER)
            ContentWorkflowAction.APPROVE_EDITORIAL ->
                roles.contains(CmsRole.EDITOR)
            ContentWorkflowAction.PUBLISH,
            ContentWorkflowAction.RETIRE,
            ->
                roles.contains(CmsRole.EDITOR) || roles.contains(CmsRole.SUPER_ADMIN)
        }
    }

    fun requiresClinicalReviewBeforePublish(risk: MedicalRiskLevel): Boolean = risk != MedicalRiskLevel.NONE
}
