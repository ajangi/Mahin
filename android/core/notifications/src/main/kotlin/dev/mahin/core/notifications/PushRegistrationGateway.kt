package dev.mahin.core.notifications

interface PushRegistrationGateway {
    suspend fun registerTokenIfNeeded(token: String): Result<Unit>
}

class NoOpPushRegistrationGateway : PushRegistrationGateway {
    override suspend fun registerTokenIfNeeded(token: String): Result<Unit> = Result.success(Unit)
}
