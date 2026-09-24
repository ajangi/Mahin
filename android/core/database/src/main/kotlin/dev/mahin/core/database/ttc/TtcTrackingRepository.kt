package dev.mahin.core.database.ttc

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

data class TtcDayLogInput(
    val logDate: LocalDate,
    val bbtCelsius: Double?,
    val ovulationTestResult: OvulationTestResult?,
    val cervicalMucus: CervicalMucusType?,
    val intercourseLogged: Boolean,
    val intercourseProtected: Boolean?,
    val pregnancyTestResult: PregnancyTestResult?,
)

@Singleton
class TtcTrackingRepository
    @Inject
    constructor(
        database: MahinDatabase,
    ) {
        private val profileDao = database.cycleProfileDao()
        private val ttcDao = database.ttcDayLogDao()

        fun observeProfile(): Flow<CycleProfileEntity?> = profileDao.observeProfile()

        fun observeTtcLogs(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<TtcDayLogEntity>> = ttcDao.observeRange(start, end)

        suspend fun getTtcLogForDate(date: LocalDate): TtcDayLogEntity? = ttcDao.getForDate(date)

        suspend fun getReproductiveMode(): ReproductiveMode =
            profileDao.getProfile()?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING

        suspend fun getTtcLogsInRange(
            start: LocalDate,
            end: LocalDate,
        ): List<TtcDayLogEntity> = ttcDao.getRange(start, end)

        suspend fun upsertTtcDayLog(input: TtcDayLogInput) {
            val existing = ttcDao.getForDate(input.logDate)
            val hasAnySignal =
                input.bbtCelsius != null ||
                    input.ovulationTestResult != null ||
                    input.cervicalMucus != null ||
                    input.intercourseLogged ||
                    input.pregnancyTestResult != null
            if (!hasAnySignal) {
                if (existing != null) {
                    ttcDao.deleteByDate(input.logDate)
                }
                return
            }
            val id = existing?.id ?: UUID.randomUUID().toString()
            ttcDao.upsert(
                TtcDayLogEntity(
                    id = id,
                    logDate = input.logDate,
                    bbtCelsius = input.bbtCelsius,
                    ovulationTestResult = input.ovulationTestResult,
                    cervicalMucus = input.cervicalMucus,
                    intercourseLogged = input.intercourseLogged,
                    intercourseProtected = input.intercourseProtected,
                    pregnancyTestResult = input.pregnancyTestResult,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun updateReproductiveMode(mode: ReproductiveMode) {
            val profile = profileDao.getProfile() ?: return
            profileDao.upsert(
                profile.copy(
                    reproductiveMode = mode,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }
