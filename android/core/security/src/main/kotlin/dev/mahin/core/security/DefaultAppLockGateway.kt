package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Singleton
class DefaultAppLockGateway
    @Inject
    constructor(
        preferencesRepository: AppLockPreferencesRepository,
    ) : AppLockGateway by AppLockGatewayEngine(
            preferencesRepository.snapshot,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
