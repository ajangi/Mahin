package dev.mahin.core.database.logging

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CustomSymptomTagEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CustomSymptomTagRepository
    @Inject
    constructor(
        database: MahinDatabase,
    ) {
        private val dao = database.customSymptomTagDao()

        fun observeAll(): Flow<List<CustomSymptomTagEntity>> = dao.observeAll()

        suspend fun upsert(entity: CustomSymptomTagEntity) {
            dao.upsert(entity)
        }
    }
