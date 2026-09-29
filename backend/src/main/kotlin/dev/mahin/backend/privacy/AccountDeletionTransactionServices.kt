package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestEntity
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditService
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

data class DeletionProcessingClaim(
    val requestId: UUID,
    val userId: UUID,
)

@Service
class AccountDeletionClaimService(
    private val deletionRequestRepository: DeletionRequestRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun claim(
        requestId: UUID,
        now: Instant,
    ): DeletionProcessingClaim? {
        val request = deletionRequestRepository.findById(requestId).orElse(null)
        if (request == null || request.status != AccountDeletionProcessor.STATUS_PENDING) {
            return null
        }
        request.status = AccountDeletionProcessor.STATUS_PROCESSING
        request.processingStartedAt = now
        request.failureReason = null
        deletionRequestRepository.saveAndFlush(request)
        return DeletionProcessingClaim(requestId = request.id, userId = request.userId)
    }
}

@Service
class AccountDeletionErasureRunner(
    private val registeredUserErasure: RegisteredUserErasure,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun eraseRegisteredUser(userId: UUID) {
        registeredUserErasure.eraseRegisteredUser(userId)
    }
}

@Service
class AccountDeletionOutcomeRecorder(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val securityAuditService: SecurityAuditService,
    @Value("\${mahin.privacy.deletion-max-attempts}") private val maxAttempts: Int,
    @Value("\${mahin.privacy.deletion-retry-base-seconds}") private val retryBaseSeconds: Long,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordSuccess(
        requestId: UUID,
        userId: UUID,
        now: Instant,
    ) {
        securityAuditService.record(
            SecurityAuditService.AuditRecord(
                actorType = "user",
                actorId = userId.toString(),
                action = "account_deletion_completed",
                targetType = "deletion_request",
                targetId = requestId.toString(),
                metadata = mapOf("completedAt" to now.toString()),
            ),
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordFailure(
        requestId: UUID,
        now: Instant,
        ex: RuntimeException,
    ) {
        val request =
            deletionRequestRepository.findById(requestId).orElse(null)
                ?: return
        applyFailureState(request, requestId, now, ex)
        deletionRequestRepository.saveAndFlush(request)
    }

    private fun applyFailureState(
        request: DeletionRequestEntity,
        requestId: UUID,
        now: Instant,
        ex: RuntimeException,
    ) {
        request.attemptCount = request.attemptCount + 1
        request.processingStartedAt = null
        request.failureReason = ex.javaClass.simpleName
        if (request.attemptCount >= maxAttempts) {
            request.status = AccountDeletionProcessor.STATUS_FAILED
            request.completedAt = now
            securityAuditService.record(
                SecurityAuditService.AuditRecord(
                    actorType = "system",
                    actorId = "account_deletion",
                    action = "account_deletion_failed_permanent",
                    targetType = "deletion_request",
                    targetId = requestId.toString(),
                    metadata =
                        mapOf(
                            "reason" to ex.javaClass.simpleName,
                            "attemptCount" to request.attemptCount.toString(),
                        ),
                ),
            )
        } else {
            request.status = AccountDeletionProcessor.STATUS_PENDING
            request.scheduledAt = now.plusSeconds(retryDelaySeconds(request.attemptCount))
            securityAuditService.record(
                SecurityAuditService.AuditRecord(
                    actorType = "system",
                    actorId = "account_deletion",
                    action = "account_deletion_failed",
                    targetType = "deletion_request",
                    targetId = requestId.toString(),
                    metadata =
                        mapOf(
                            "reason" to ex.javaClass.simpleName,
                            "attemptCount" to request.attemptCount.toString(),
                            "retryAt" to request.scheduledAt.toString(),
                        ),
                ),
            )
        }
    }

    private fun retryDelaySeconds(attemptCount: Int): Long =
        retryBaseSeconds * (1L shl (attemptCount - 1).coerceAtMost(6))
}
