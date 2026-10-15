package dev.mahin.core.database.pregnancy

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.PregnancyChecklistItemEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class PregnancyChecklistRepository
    @Inject
    constructor(
        database: MahinDatabase,
    ) {
        private val dao = database.pregnancyChecklistDao()

        fun observeHospitalBag(): Flow<List<PregnancyChecklistItemEntity>> = dao.observeByKind(HOSPITAL_BAG_KIND)

        suspend fun seedFromTemplateIfEmpty(items: List<TemplateItem>) {
            if (dao.countByKind(HOSPITAL_BAG_KIND) > 0) return
            val now = System.currentTimeMillis()
            val entities =
                items.map { item ->
                    PregnancyChecklistItemEntity(
                        id = UUID.randomUUID().toString(),
                        checklistKind = HOSPITAL_BAG_KIND,
                        section = item.section,
                        label = item.label,
                        sortOrder = item.order,
                        checked = false,
                        templateItemId = item.templateId,
                        updatedAtEpochMs = now,
                    )
                }
            if (entities.isNotEmpty()) {
                dao.upsertAll(entities)
            }
        }

        suspend fun seedFromTemplate(
            items: List<TemplateItem>,
        ) {
            if (items.isEmpty()) return
            val now = System.currentTimeMillis()
            dao.deleteByKind(HOSPITAL_BAG_KIND)
            dao.upsertAll(
                items.map { item ->
                    PregnancyChecklistItemEntity(
                        id = UUID.randomUUID().toString(),
                        checklistKind = HOSPITAL_BAG_KIND,
                        section = item.section,
                        label = item.label,
                        sortOrder = item.order,
                        checked = false,
                        templateItemId = item.templateId,
                        updatedAtEpochMs = now,
                    )
                },
            )
        }

        suspend fun setChecked(
            id: String,
            checked: Boolean,
        ) {
            dao.setChecked(id, checked, System.currentTimeMillis())
        }

        data class TemplateItem(
            val templateId: String,
            val section: String,
            val label: String,
            val order: Int,
        )

        companion object {
            const val HOSPITAL_BAG_KIND = "hospital_bag"
        }
    }
