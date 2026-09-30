package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AccountDeletionProcessor(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val accountDeletionExecutor: AccountDeletionExecutor,
    @Value("\${mahin.privacy.deletion-grace-seconds}") private val graceSeconds: Long,
    @Value("\${mahin.privacy.deletion-processing-timeout-seconds}") private val processingTimeoutSeconds: Long,
) {
    fun processDueDeletions(now: Instant = Instant.now()): Int {
        recoverStuckProcessing(now)
        val due =
            deletionRequestRepository.findAllByStatusAndScheduledAtLessThanEqual(
                status = STATUS_PENDING,
                scheduledAt = now,
            )
        var processed = 0
        due.forEach { request ->
            try {
                if (accountDeletionExecutor.processSingle(request.id, now)) {
                    processed++
                }
            } catch (_: RuntimeException) {
                // Outcome should be persisted in a separate transaction; swallow to continue batch.
            }
        }
        return processed
    }

    fun recoverStuckProcessing(now: Instant) {
        val stuckBefore = now.minusSeconds(processingTimeoutSeconds)
        val stuck =
            deletionRequestRepository.findAllByStatusAndProcessingStartedAtBefore(
                status = STATUS_PROCESSING,
                processingStartedAt = stuckBefore,
            )
        stuck.forEach { request ->
            request.status = STATUS_PENDING
            request.processingStartedAt = null
            deletionRequestRepository.save(request)
        }
    }

    fun gracePeriodSeconds(): Long = graceSeconds

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_PROCESSING = "processing"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_FAILED = "failed"
    }
}
