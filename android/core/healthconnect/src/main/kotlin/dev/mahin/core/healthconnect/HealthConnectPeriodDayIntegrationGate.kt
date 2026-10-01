package dev.mahin.core.healthconnect

import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * True when remote launch flag is on and the user has opted into Health Connect sync.
 * Core tracker tombstone/delete behaviour applies only in this state.
 */
interface HealthConnectPeriodDayIntegrationGate {
    suspend fun isIntegrationActive(): Boolean
}

@Singleton
class DefaultHealthConnectPeriodDayIntegrationGate
    @Inject
    constructor(
        private val featureFlagGateway: FeatureFlagGateway,
        private val preferencesRepository: HealthConnectPreferencesRepository,
    ) : HealthConnectPeriodDayIntegrationGate {
        override suspend fun isIntegrationActive(): Boolean {
            if (!featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)) return false
            return preferencesRepository.snapshot.first().userOptIn
        }
    }
