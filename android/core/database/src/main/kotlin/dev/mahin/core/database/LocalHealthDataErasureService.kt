package dev.mahin.core.database

import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalHealthDataErasureService
    @Inject
    constructor(
        private val databaseProvider: MahinDatabaseProvider,
        private val healthConnectPeriodDayTombstoneRepository: HealthConnectPeriodDayTombstoneRepository,
        private val healthConnectPreferencesRepository: HealthConnectPreferencesRepository,
    ) {
        suspend fun eraseAllLocalHealthData() =
            withContext(Dispatchers.IO) {
                val database = databaseProvider.database()
                database.runInTransaction {
                    database.clearAllTables()
                }
                healthConnectPeriodDayTombstoneRepository.clearAll()
                healthConnectPreferencesRepository.clearIntegrationState()
            }
    }
