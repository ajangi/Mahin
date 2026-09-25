package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestEntity
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.privacy.persistence.ExportJobEntity
import dev.mahin.backend.privacy.persistence.ExportJobRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrivacyAccountService(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val exportJobRepository: ExportJobRepository,
) {
    @Transactional
    fun requestDeletion(userId: UUID): DeletionRequestResponse {
        val now = Instant.now()
        val entity =
            deletionRequestRepository.save(
                DeletionRequestEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    status = "pending",
                    requestedAt = now,
                    scheduledAt = now.plusSeconds(DELETION_GRACE_SECONDS),
                ),
            )
        return entity.toResponse()
    }

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
        )

    private fun ExportJobEntity.toResponse() =
        ExportJobResponse(
            id = id,
            status = status,
            requestedAt = requestedAt,
            completedAt = completedAt,
        )

    companion object {
        private const val DELETION_GRACE_SECONDS = 14L * 24 * 3600
    }
}
