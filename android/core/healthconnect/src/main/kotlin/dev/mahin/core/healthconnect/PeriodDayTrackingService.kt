package dev.mahin.core.healthconnect

import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodDayTrackingService
    @Inject
    constructor(
        private val cycleTrackingRepository: CycleTrackingRepository,
        private val tombstoneRepository: HealthConnectPeriodDayTombstoneRepository,
    ) {
        suspend fun saveLoggedPeriodDay(
            date: LocalDate,
            flowLevel: dev.mahin.core.model.PeriodFlowLevel?,
            hasClots: Boolean,
        ) {
            tombstoneRepository.clearUserDeleted(date)
            cycleTrackingRepository.upsertPeriodDay(
                date = date,
                flowLevel = flowLevel,
                hasClots = hasClots,
            )
        }

        suspend fun removeUserPeriodDay(date: LocalDate) {
            cycleTrackingRepository.deletePeriodDay(date)
            tombstoneRepository.markUserDeleted(date)
        }
    }
