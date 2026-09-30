package dev.mahin.backend.privacy

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.transaction.support.TransactionTemplate

object TransactionalErasureFailureSupport {
    val failUserIds: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
}

class TransactionalTestRegisteredUserErasure(
    private val delegate: UserDataErasureService,
    private val transactionTemplate: TransactionTemplate,
) : RegisteredUserErasure {
    override fun eraseRegisteredUser(userId: UUID) {
        transactionTemplate.executeWithoutResult {
            if (TransactionalErasureFailureSupport.failUserIds.contains(userId)) {
                error("boom")
            }
            delegate.eraseRegisteredUser(userId)
        }
    }
}

@TestConfiguration
class TransactionalErasureTestConfiguration {
    @Bean
    @Primary
    fun registeredUserErasure(
        delegate: UserDataErasureService,
        transactionTemplate: TransactionTemplate,
    ): RegisteredUserErasure = TransactionalTestRegisteredUserErasure(delegate, transactionTemplate)
}
