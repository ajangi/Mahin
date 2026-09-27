package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.api.currentCmsRoles
import dev.mahin.backend.api.currentCmsStaff
import dev.mahin.backend.cms.CmsRole
import dev.mahin.backend.content.persistence.ContentAuditEventRepository
import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentSourceEntity
import dev.mahin.backend.content.persistence.ContentSourceRepository
import dev.mahin.backend.content.persistence.ContentVersionEntity
import dev.mahin.backend.content.persistence.ContentVersionRepository
import dev.mahin.backend.content.persistence.ContentVersionSourceEntity
import dev.mahin.backend.content.persistence.ContentVersionSourceRepository
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Suppress("LongParameterList")
class ContentAdminService(
    private val documentRepository: ContentDocumentRepository,
    private val versionRepository: ContentVersionRepository,
    private val sourceRepository: ContentSourceRepository,
    private val versionSourceRepository: ContentVersionSourceRepository,
    private val catalogService: ContentCatalogService,
    private val auditService: ContentAuditService,
    private val auditRepository: ContentAuditEventRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createDocument(request: CreateContentDocumentRequest): AdminContentVersionResponse {
        requireEditor()
        if (documentRepository.findBySlugAndLocale(request.slug, request.locale).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "slug_exists")
        }
        val now = Instant.now()
        val document =
            documentRepository.save(
                ContentDocumentEntity(
                    slug = request.slug,
                    locale = request.locale,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        val version = newVersion(document, request, versionNumber = 1)
        linkSources(version, request.sourceIds)
        auditService.record(currentCmsStaff(), "document_created", document.id, version.id)
        return toAdminResponse(document, version)
    }

    @Transactional
    fun createRevision(
        documentId: UUID,
        request: CreateContentDocumentRequest,
    ): AdminContentVersionResponse {
        requireEditor()
        val document =
            documentRepository.findById(documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "document_not_found")
            }
        val latest =
            versionRepository.findTopByDocumentIdOrderByVersionNumberDesc(document.id).orElseThrow()
        val version = newVersion(document, request, versionNumber = latest.versionNumber + 1)
        linkSources(version, request.sourceIds)
        document.updatedAt = Instant.now()
        documentRepository.save(document)
        auditService.record(currentCmsStaff(), "revision_created", document.id, version.id)
        return toAdminResponse(document, version)
    }

    fun getVersion(versionId: UUID): AdminContentVersionResponse {
        val version =
            versionRepository.findById(versionId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "version_not_found")
            }
        val document =
            documentRepository.findById(version.documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "document_not_found")
            }
        return toAdminResponse(document, version)
    }

    @Transactional
    fun transition(
        versionId: UUID,
        action: ContentWorkflowAction,
        request: WorkflowTransitionRequest,
    ): AdminContentVersionResponse {
        val roles = currentCmsRoles()
        if (!ContentWorkflowPolicy.canPerform(roles, action)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "workflow_forbidden")
        }
        val version =
            versionRepository.findById(versionId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "version_not_found")
            }
        val document =
            documentRepository.findById(version.documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "document_not_found")
            }
        val actor = currentCmsStaff()
        ContentWorkflowTransitions.requireLegal(document, version, action)
        when (action) {
            ContentWorkflowAction.SUBMIT_MEDICAL_REVIEW -> {
                version.status = "review"
                version.reviewStage = ReviewStage.MEDICAL.name.lowercase()
            }
            ContentWorkflowAction.APPROVE_MEDICAL -> {
                version.clinicalReviewer = request.clinicalReviewer ?: actor.email
                version.clinicalReviewedAt = Instant.now()
                request.nextReviewDueAt?.let {
                    version.nextReviewDueAt = Instant.parse(it)
                }
            }
            ContentWorkflowAction.SUBMIT_EDITORIAL_REVIEW -> {
                version.status = "review"
                version.reviewStage = ReviewStage.EDITORIAL.name.lowercase()
            }
            ContentWorkflowAction.APPROVE_EDITORIAL -> {
                version.status = "approved"
                version.reviewStage = null
            }
            ContentWorkflowAction.PUBLISH -> publish(document, version)
            ContentWorkflowAction.RETIRE -> retire(document, version)
        }
        versionRepository.save(version)
        document.updatedAt = Instant.now()
        documentRepository.save(document)
        auditService.record(actor, action.name.lowercase(), document.id, version.id)
        return toAdminResponse(document, version)
    }

    @Transactional
    fun createSource(request: CreateContentSourceRequest): ContentSourceResponse {
        requireEditor()
        if (sourceRepository.findByCitationKey(request.citationKey).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "citation_key_exists")
        }
        val now = Instant.now()
        val entity =
            sourceRepository.save(
                ContentSourceEntity(
                    citationKey = request.citationKey,
                    title = request.title,
                    url = request.url,
                    publicationDate = request.publicationDate?.let { java.time.LocalDate.parse(it) },
                    notes = request.notes,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        auditService.record(currentCmsStaff(), "source_created", metadata = mapOf("sourceId" to entity.id.toString()))
        return ContentMapper.toSourceResponse(entity)
    }

    fun listSources(): List<ContentSourceResponse> =
        sourceRepository.findAll().map { ContentMapper.toSourceResponse(it) }

    fun freshnessDashboard(): ContentFreshnessDashboardResponse {
        requireAnalystOrEditor()
        val now = Instant.now()
        val staleThreshold = now.minusSeconds(180L * 24 * 3600)
        val versions = versionRepository.findAll()
        val overdue =
            versions.filter { v ->
                v.nextReviewDueAt != null &&
                    v.nextReviewDueAt!!.isBefore(now) &&
                    (v.status == "published" || v.status == "approved")
            }
        val sources = sourceRepository.findAll()
        val staleSources =
            sources.filter { source ->
                source.lastCheckedAt == null || source.lastCheckedAt!!.isBefore(staleThreshold)
            }
        val withdrawn =
            documentRepository.findAll().filter { it.withdrawnAt != null }.take(20)
        return ContentFreshnessDashboardResponse(
            overdueClinicalReview =
                overdue.map {
                    FreshnessItem(
                        documentId = it.documentId.toString(),
                        versionId = it.id.toString(),
                        sourceId = null,
                        title = it.title,
                        detail = "next_review_due_overdue",
                    )
                },
            staleSources =
                staleSources.map {
                    FreshnessItem(
                        documentId = null,
                        versionId = null,
                        sourceId = it.id.toString(),
                        title = it.title,
                        detail = "source_last_checked_stale",
                    )
                },
            recentlyWithdrawn =
                withdrawn.map {
                    FreshnessItem(
                        documentId = it.id.toString(),
                        versionId = it.publishedVersionId?.toString(),
                        sourceId = null,
                        title = it.slug,
                        detail = "withdrawn_at_${it.withdrawnAt}",
                    )
                },
        )
    }

    fun auditForDocument(documentId: UUID): ContentAuditListResponse {
        requireAnalystOrEditor()
        val events = auditRepository.findByDocumentIdOrderByCreatedAtDesc(documentId)
        return ContentAuditListResponse(
            events =
                events.map {
                    ContentAuditEventResponse(
                        id = it.id.toString(),
                        action = it.action,
                        actorEmail = it.actorEmail,
                        documentId = it.documentId?.toString(),
                        versionId = it.versionId?.toString(),
                        createdAt = DateTimeFormatter.ISO_INSTANT.format(it.createdAt),
                    )
                },
        )
    }

    private fun publish(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
    ) {
        val risk = ContentMapper.parseMedicalRiskLevel(version.medicalRiskLevel)
        if (ContentWorkflowPolicy.requiresClinicalReviewBeforePublish(risk) &&
            version.clinicalReviewedAt == null
        ) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "clinical_review_required")
        }
        if (risk != MedicalRiskLevel.NONE) {
            val linkedSources = versionSourceRepository.findByVersionId(version.id)
            if (linkedSources.isEmpty()) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "sources_required")
            }
        }
        version.status = "published"
        version.effectiveFrom = Instant.now()
        document.publishedVersionId = version.id
        document.withdrawnAt = null
        catalogService.bumpPublicationRevision()
    }

    private fun retire(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
    ) {
        version.status = "retired"
        version.effectiveTo = Instant.now()
        document.withdrawnAt = Instant.now()
        document.publishedVersionId = null
        catalogService.bumpPublicationRevision()
    }

    private fun newVersion(
        document: ContentDocumentEntity,
        request: CreateContentDocumentRequest,
        versionNumber: Int,
    ): ContentVersionEntity {
        val version =
            versionRepository.save(
                ContentVersionEntity(
                    documentId = document.id,
                    versionNumber = versionNumber,
                    title = request.title,
                    searchIndexText = ContentSearchIndex.build(request.title, request.summary),
                    summary = request.summary,
                    bodyRichtext = request.bodyRichtext,
                    contentType = request.contentType,
                    lifeStage = request.lifeStage,
                    gestationalWeek = request.gestationalWeek,
                    tagsJson = objectMapper.writeValueAsString(request.tags),
                    medicalRiskLevel = request.medicalRiskLevel.wireValue(),
                    status = "draft",
                    createdByStaffId = currentCmsStaff().staffId,
                ),
            )
        return version
    }

    private fun linkSources(
        version: ContentVersionEntity,
        sourceIds: List<String>,
    ) {
        versionSourceRepository.deleteByVersionId(version.id)
        sourceIds.forEach { raw ->
            val sourceId = UUID.fromString(raw)
            if (!sourceRepository.existsById(sourceId)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "source_not_found")
            }
            versionSourceRepository.save(
                ContentVersionSourceEntity(versionId = version.id, sourceId = sourceId),
            )
        }
    }

    private fun toAdminResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
    ): AdminContentVersionResponse {
        val sourceIds = versionSourceRepository.findByVersionId(version.id).map { it.sourceId }
        return ContentMapper.toAdminVersionResponse(document, version, sourceIds)
    }

    private fun requireEditor() {
        val roles = currentCmsRoles()
        if (!roles.contains(CmsRole.EDITOR) && !roles.contains(CmsRole.SUPER_ADMIN)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "editor_required")
        }
    }

    private fun requireAnalystOrEditor() {
        val roles = currentCmsRoles()
        if (roles.none {
                it in
                    setOf(
                        CmsRole.ANALYST,
                        CmsRole.EDITOR,
                        CmsRole.MEDICAL_REVIEWER,
                        CmsRole.SUPER_ADMIN,
                    )
            }
        ) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "dashboard_forbidden")
        }
    }
}
