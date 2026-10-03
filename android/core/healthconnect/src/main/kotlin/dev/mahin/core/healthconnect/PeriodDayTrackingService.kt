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
        suspend fun removeUserPeriodDay(date: LocalDate) {
            cycleTrackingRepository.deletePeriodDay(date)
            tombstoneRepository.markUserDeleted(date)
        }

        /**
         * Applies period-day logging from the daily log screen.
         * Tombstones and row deletion run only when [healthConnectIntegrationActive] is true.
         */
        suspend fun applyUserPeriodLogChange(
            healthConnectIntegrationActive: Boolean,
            loggingPeriod: Boolean,
            date: LocalDate,
            flowLevel: dev.mahin.core.model.PeriodFlowLevel?,
            ensurePeriodSpanExists: suspend () -> Unit,
        ) {
            if (loggingPeriod) {
                ensurePeriodSpanExists()
                tombstoneRepository.clearUserDeleted(date)
                cycleTrackingRepository.upsertPeriodDay(
                    date = date,
                    flowLevel = flowLevel,
                    hasClots = false,
                )
            } else if (healthConnectIntegrationActive && cycleTrackingRepository.getPeriodDayForDate(date) != null) {
                removeUserPeriodDay(date)
            }
        }
    }
