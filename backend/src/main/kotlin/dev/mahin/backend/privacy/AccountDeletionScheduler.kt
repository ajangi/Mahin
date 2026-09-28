package dev.mahin.backend.privacy

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AccountDeletionScheduler(
    private val accountDeletionProcessor: AccountDeletionProcessor,
) {
    @Scheduled(fixedDelayString = "\${mahin.privacy.deletion-processor-delay-ms:60000}")
    fun processDueDeletions() {
        accountDeletionProcessor.processDueDeletions()
    }
}
