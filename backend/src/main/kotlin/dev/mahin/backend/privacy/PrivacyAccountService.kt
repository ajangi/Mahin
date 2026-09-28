package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestEntity
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.privacy.persistence.ExportJobEntity
import dev.mahin.backend.privacy.persistence.ExportJobRepository
import dev.mahin.backend.security.SecurityAuditService
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrivacyAccountService(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val exportJobRepository: ExportJobRepository,
    private val accountDeletionProcessor: AccountDeletionProcessor,
    private val securityAuditService: SecurityAuditService,
) {
    @Transactional
    fun requestDeletion(userId: UUID): DeletionRequestResponse {
        val now = Instant.now()
        val graceSeconds = accountDeletionProcessor.gracePeriodSeconds()
        val entity =
            deletionRequestRepository.save(
                DeletionRequestEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    status = AccountDeletionProcessor.STATUS_PENDING,
                    requestedAt = now,
                    scheduledAt =
                        if (graceSeconds <= 0) {
                            now.minusSeconds(1)
                        } else {
                            now.plusSeconds(graceSeconds)
                        },
                ),
            )
        securityAuditService.record(
            SecurityAuditService.AuditRecord(
                actorType = "user",
                actorId = userId.toString(),
                action = "account_deletion_requested",
                targetType = "deletion_request",
                targetId = entity.id.toString(),
            ),
        )
        return entity.toResponse()
    }

    @Transactional(readOnly = true)
    fun latestDeletionRequest(userId: UUID): DeletionRequestResponse? =
        deletionRequestRepository.findTopByUserIdOrderByRequestedAtDesc(userId)?.toResponse()

    @Transactional
    fun requestExport(userId: UUID): ExportJobResponse {
        val now = Instant.now()
        val entity =
            exportJobRepository.save(
                ExportJobEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    status = "queued",
                    requestedAt = now,
                ),
            )
        return entity.toResponse()
    }

    @Transactional(readOnly = true)
    @Suppress("ReturnCount")
    fun getExportJob(
        userId: UUID,
        jobId: UUID,
    ): ExportJobResponse? {
        val job = exportJobRepository.findById(jobId).orElse(null) ?: return null
        if (job.userId != userId) {
            return null
        }
        return job.toResponse()
    }

    private fun DeletionRequestEntity.toResponse() =
        DeletionRequestResponse(
            id = id,
            status = status,
            requestedAt = requestedAt,
            scheduledAt = scheduledAt,
            completedAt = completedAt,
        )

    private fun ExportJobEntity.toResponse() =
        ExportJobResponse(
            id = id,
            status = status,
            requestedAt = requestedAt,
            completedAt = completedAt,
        )
}
