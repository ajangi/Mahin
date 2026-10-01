package dev.mahin.core.config

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class FeatureFlagRepository
    @Inject
    constructor(
        private val metaApi: MetaApi,
    ) {
        private val mutex = Mutex()
        private var cached: Map<String, Boolean> = emptyMap()

        suspend fun refreshFromRemote(): Result<Unit> =
            runCatching {
                val response = metaApi.meta()
                mutex.withLock {
                    cached = response.featureFlags
                }
            }

        fun snapshot(): Map<String, Boolean> = cached

        fun isEnabled(flag: String): Boolean = cached[flag] == true
    }

@Singleton
class RemoteFeatureFlagGateway
    @Inject
    constructor(
        private val repository: FeatureFlagRepository,
    ) : FeatureFlagGateway {
        override fun isEnabled(flag: String): Boolean = repository.isEnabled(flag)
    }
