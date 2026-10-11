package dev.mahin.core.database.cycle

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodDayEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.domain.cycle.CyclePredictionEngineV1
import dev.mahin.domain.cycle.CyclePredictionInput
import dev.mahin.domain.cycle.CyclePredictionResult
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CycleDashboard(
    val profile: CycleProfileEntity?,
    val prediction: CyclePredictionResult,
    val todayLog: DailyLogEntity?,
    val onPeriodToday: Boolean,
    val openPeriodStart: LocalDate?,
)

@Singleton
class CycleTrackingRepository
    @Inject
    constructor(
        database: MahinDatabase,
    ) {
        private val profileDao = database.cycleProfileDao()
        private val periodDao = database.periodRecordDao()
        private val periodDayDao = database.periodDayDao()
        private val dailyLogDao = database.dailyLogDao()

        fun observeDashboard(today: LocalDate = LocalDate.now()): Flow<CycleDashboard> =
            combine(
                profileDao.observeProfile(),
                periodDao.observeAll(),
                dailyLogDao.observeForDate(today),
            ) { profile, periods, todayLog ->
                val prediction = buildPrediction(today, profile, periods)
                val (openStart, _) = CyclePeriodMapper.openPeriod(periods)
                val onPeriod =
                    periods.any { record ->
                        !today.isBefore(record.startDate) &&
                            (record.endDate == null || !today.isAfter(record.endDate))
                    }
                CycleDashboard(
                    profile = profile,
                    prediction = prediction,
                    todayLog = todayLog,
                    onPeriodToday = onPeriod,
                    openPeriodStart = openStart,
                )
            }

        fun observePeriods(): Flow<List<PeriodRecordEntity>> = periodDao.observeAll()

        fun observeDailyLogs(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<DailyLogEntity>> = dailyLogDao.observeRange(start, end)

        fun observePeriodDays(
            start: LocalDate,
            end: LocalDate,
        ) = periodDayDao.observeRange(start, end)

        suspend fun getProfile(): CycleProfileEntity? = profileDao.getProfile()

        suspend fun completeOnboarding(input: CycleOnboardingInput) {
            val now = System.currentTimeMillis()
            profileDao.upsert(
                CycleProfileEntity(
                    reproductiveMode = input.mode,
                    typicalCycleLengthDays = input.typicalCycleLengthDays,
                    typicalPeriodLengthDays = input.typicalPeriodLengthDays,
                    regularity = input.regularity,
                    onboardingCompleted = true,
                    updatedAtEpochMs = now,
                ),
            )
            val orderedStarts = (input.priorPeriodStarts + input.lastPeriodStart).distinct().sorted()
            orderedStarts.forEachIndexed { index, start ->
                val isLast = index == orderedStarts.lastIndex
                val end =
                    if (isLast) {
                        input.lastPeriodEnd
                    } else {
                        start.plusDays((input.typicalPeriodLengthDays ?: 5) - 1L)
                    }
                upsertPeriod(
                    startDate = start,
                    endDate = end,
                    note = null,
                    recordId = null,
                )
            }
        }

        suspend fun upsertPeriod(
            startDate: LocalDate,
            endDate: LocalDate?,
            note: String?,
            recordId: String?,
        ): String {
            val now = System.currentTimeMillis()
            val id = recordId ?: UUID.randomUUID().toString()
            periodDao.upsert(
                PeriodRecordEntity(
                    id = id,
                    startDate = startDate,
                    endDate = endDate,
                    note = note,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
            return id
        }

        suspend fun deletePeriod(id: String) {
            periodDao.deleteById(id)
        }

        suspend fun upsertPeriodDay(
            date: LocalDate,
            flowLevel: PeriodFlowLevel?,
            hasClots: Boolean,
            updatedAtEpochMs: Long = System.currentTimeMillis(),
        ) {
            periodDayDao.upsert(
                PeriodDayEntity(
                    logDate = date,
                    flowLevel = flowLevel,
                    hasClots = hasClots,
                    updatedAtEpochMs = updatedAtEpochMs,
                ),
            )
        }

        suspend fun deletePeriodDay(date: LocalDate) {
            periodDayDao.deleteByDate(date)
        }

        suspend fun getPeriodDaysInRange(
            start: LocalDate,
            end: LocalDate,
        ): List<PeriodDayEntity> = periodDayDao.getRange(start, end)

        @Suppress("LongParameterList")
        suspend fun upsertDailyLog(
            date: LocalDate,
            moodTags: Set<String>,
            symptomTags: Set<String>,
            painSeverity: Int?,
            painLocation: String? = null,
            painNote: String? = null,
            note: String?,
        ) {
            val id = dailyLogDao.getForDate(date)?.id ?: UUID.randomUUID().toString()
            dailyLogDao.upsert(
                DailyLogEntity(
                    id = id,
                    logDate = date,
                    moodTags = moodTags.joinToString(","),
                    symptomTags = symptomTags.joinToString(","),
                    painSeverity = painSeverity,
                    painLocation = painLocation,
                    painNote = painNote,
                    note = note,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun mergeAdjacentPeriods(
            firstId: String,
            secondId: String,
        ) {
            val first = periodDao.getById(firstId) ?: return
            val second = periodDao.getById(secondId) ?: return
            val firstEnd = first.endDate ?: first.startDate
            if (!firstEnd.plusDays(1).equals(second.startDate)) return
            val now = System.currentTimeMillis()
            periodDao.upsert(
                first.copy(
                    endDate = second.endDate,
                    updatedAtEpochMs = now,
                ),
            )
            periodDao.deleteById(secondId)
        }

        suspend fun splitPeriodRecord(
            recordId: String,
            splitAfterDate: LocalDate,
        ) {
            val record = periodDao.getById(recordId) ?: return
            val end = record.endDate ?: return
            if (splitAfterDate.isBefore(record.startDate) || !splitAfterDate.isBefore(end)) return
            val now = System.currentTimeMillis()
            periodDao.upsert(
                record.copy(
                    endDate = splitAfterDate,
                    updatedAtEpochMs = now,
                ),
            )
            periodDao.upsert(
                PeriodRecordEntity(
                    id = UUID.randomUUID().toString(),
                    startDate = splitAfterDate.plusDays(1),
                    endDate = end,
                    note = record.note,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
        }

        suspend fun getAllPeriods(): List<PeriodRecordEntity> = periodDao.getAll()

        suspend fun getDailyLogForDate(date: LocalDate): DailyLogEntity? = dailyLogDao.getForDate(date)

        suspend fun getPeriodDayForDate(date: LocalDate): PeriodDayEntity? = periodDayDao.getForDate(date)

        private fun buildPrediction(
            today: LocalDate,
            profile: CycleProfileEntity?,
            periods: List<PeriodRecordEntity>,
        ): CyclePredictionResult {
            val completed = CyclePeriodMapper.toCompletedCycles(periods.filter { it.endDate != null })
            val (openStart, openEnd) = CyclePeriodMapper.openPeriod(periods)
            return CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = today,
                    completedCycles = completed,
                    openPeriodStart = openStart,
                    openPeriodEnd = openEnd,
                    typicalCycleLengthDays = profile?.typicalCycleLengthDays,
                    typicalPeriodLengthDays = profile?.typicalPeriodLengthDays,
                    regularity = profile?.regularity ?: CycleRegularity.UNKNOWN,
                ),
            )
        }
    }
