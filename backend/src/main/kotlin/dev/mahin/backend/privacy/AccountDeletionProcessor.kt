package dev.mahin.backend.privacy

import dev.mahin.backend.privacy.persistence.DeletionRequestEntity
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditService
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountDeletionProcessor(
    private val deletionRequestRepository: DeletionRequestRepository,
    private val userDataErasureService: UserDataErasureService,
    private val securityAuditService: SecurityAuditService,
    @Value("\${mahin.privacy.deletion-grace-seconds}") private val graceSeconds: Long,
) {
    fun processDueDeletions(now: Instant = Instant.now()): Int {
        val due =
            deletionRequestRepository.findAllByStatusAndScheduledAtLessThanEqual(
                status = STATUS_PENDING,
                scheduledAt = now,
            )
        var processed = 0
        due.forEach { request ->
            if (processSingle(request, now)) {
                processed++
            }
        }
        return processed
    }

    @Transactional
    @Suppress("TooGenericExceptionCaught")
    fun processSingle(
        request: DeletionRequestEntity,
        now: Instant = Instant.now(),
    ): Boolean {
        if (request.status != STATUS_PENDING) return false
        val requestId = request.id
        val userId = request.userId
        request.status = STATUS_PROCESSING
        deletionRequestRepository.save(request)
        return try {
            userDataErasureService.eraseRegisteredUser(userId)
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
            request.status = STATUS_FAILED
            request.failureReason = ex.javaClass.simpleName
            request.completedAt = now
            deletionRequestRepository.save(request)
            securityAuditService.record(
                SecurityAuditService.AuditRecord(
                    actorType = "system",
                    actorId = "account_deletion",
                    action = "account_deletion_failed",
                    targetType = "deletion_request",
                    targetId = requestId.toString(),
                    metadata = mapOf("reason" to ex.javaClass.simpleName),
                ),
            )
            false
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
