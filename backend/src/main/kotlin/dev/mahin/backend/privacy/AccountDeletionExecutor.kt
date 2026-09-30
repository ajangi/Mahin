package dev.mahin.backend.privacy

import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class AccountDeletionExecutor(
    private val claimService: AccountDeletionClaimService,
    private val erasureRunner: AccountDeletionErasureRunner,
    private val outcomeRecorder: AccountDeletionOutcomeRecorder,
) {
    @Suppress("TooGenericExceptionCaught")
    fun processSingle(
        requestId: UUID,
        now: Instant = Instant.now(),
    ): Boolean {
        val claim = claimService.claim(requestId, now) ?: return false
        return try {
            erasureRunner.eraseRegisteredUser(claim.userId)
            outcomeRecorder.recordSuccess(claim.requestId, claim.userId, now)
            true
        } catch (ex: RuntimeException) {
            outcomeRecorder.recordFailure(claim.requestId, now, ex)
            false
        }
    }
}
