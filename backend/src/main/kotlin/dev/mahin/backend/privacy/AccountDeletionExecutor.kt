package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestEntity
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditService
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountDeletionExecutor(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val registeredUserErasure: RegisteredUserErasure,
    private val securityAuditService: SecurityAuditService,
    @Value("\${mahin.privacy.deletion-max-attempts}") private val maxAttempts: Int,
    @Value("\${mahin.privacy.deletion-retry-base-seconds}") private val retryBaseSeconds: Long,
) {
    @Transactional
    @Suppress("TooGenericExceptionCaught")
    fun processSingle(
        requestId: UUID,
        now: Instant = Instant.now(),
    ): Boolean {
        val request = deletionRequestRepository.findById(requestId).orElse(null)
        if (request == null || request.status != AccountDeletionProcessor.STATUS_PENDING) {
            return false
        }
        val userId = request.userId
        request.status = AccountDeletionProcessor.STATUS_PROCESSING
        request.processingStartedAt = now
        request.failureReason = null
        deletionRequestRepository.save(request)
        return try {
            registeredUserErasure.eraseRegisteredUser(userId)
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
            true
        } catch (ex: RuntimeException) {
            handleFailure(request, requestId, now, ex)
            false
        }
    }

    private fun handleFailure(
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
            deletionRequestRepository.save(request)
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
            deletionRequestRepository.save(request)
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
