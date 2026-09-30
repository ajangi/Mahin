package dev.mahin.core.database

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalHealthDataErasureService
    @Inject
    constructor(
        private val databaseProvider: MahinDatabaseProvider,
    ) {
        suspend fun eraseAllLocalHealthData() =
            withContext(Dispatchers.IO) {
                val database = databaseProvider.database()
                database.runInTransaction {
                    database.clearAllTables()
                }
            }
    }
