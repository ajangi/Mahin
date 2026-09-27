package dev.mahin.backend.cms

import dev.mahin.backend.content.AdminContentVersionResponse
import dev.mahin.backend.content.ContentAdminService
import dev.mahin.backend.content.ContentAuditListResponse
import dev.mahin.backend.content.ContentFreshnessDashboardResponse
import dev.mahin.backend.content.ContentWorkflowAction
import dev.mahin.backend.content.CreateContentDocumentRequest
import dev.mahin.backend.content.WorkflowTransitionRequest
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/content")
class CmsContentController(
    private val contentAdminService: ContentAdminService,
) {
    @PostMapping("/documents")
    fun createDocument(
        @Valid @RequestBody request: CreateContentDocumentRequest,
    ): AdminContentVersionResponse = contentAdminService.createDocument(request)

    @PostMapping("/documents/{documentId}/versions")
    fun createRevision(
        @PathVariable documentId: UUID,
        @Valid @RequestBody request: CreateContentDocumentRequest,
    ): AdminContentVersionResponse = contentAdminService.createRevision(documentId, request)

    @GetMapping("/versions/{versionId}")
    fun getVersion(
        @PathVariable versionId: UUID,
    ): AdminContentVersionResponse = contentAdminService.getVersion(versionId)

    @PostMapping("/versions/{versionId}/workflow/{action}")
    fun workflow(
        @PathVariable versionId: UUID,
        @PathVariable action: String,
        @RequestBody(required = false) request: WorkflowTransitionRequest?,
    ): AdminContentVersionResponse {
        val workflowAction = ContentWorkflowAction.valueOf(action.uppercase().replace('-', '_'))
        return contentAdminService.transition(versionId, workflowAction, request ?: WorkflowTransitionRequest())
    }

    @GetMapping("/freshness")
    fun freshness(): ContentFreshnessDashboardResponse = contentAdminService.freshnessDashboard()

    @GetMapping("/documents/{documentId}/audit")
    fun audit(
        @PathVariable documentId: UUID,
    ): ContentAuditListResponse = contentAdminService.auditForDocument(documentId)
}
