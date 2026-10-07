package dev.mahin.core.database.pregnancy

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.ContractionEventEntity
import dev.mahin.core.database.entity.ContractionSessionEntity
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.KickEventEntity
import dev.mahin.core.database.entity.KickSessionEntity
import dev.mahin.core.database.entity.PregnancyAppointmentEntity
import dev.mahin.core.database.entity.PregnancyDatingRevisionEntity
import dev.mahin.core.database.entity.PregnancyDayLogEntity
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

data class PregnancyDayLogInput(
    val logDate: LocalDate,
    val symptomTags: Set<String>,
    val weightKg: Double?,
    val bpSystolic: Int?,
    val bpDiastolic: Int?,
    val note: String?,
)

data class PregnancyAppointmentInput(
    val id: String?,
    val pregnancyId: String,
    val appointmentType: PregnancyAppointmentType,
    val title: String,
    val scheduledAtEpochMs: Long,
    val location: String?,
    val clinicianName: String?,
    val note: String?,
    val reminderEnabled: Boolean,
)

@Singleton
class PregnancyTrackingRepository
    @Inject
    constructor(
        database: MahinDatabase,
        private val timerPreferences: PregnancyTimerPreferencesRepository,
    ) {
        private val profileDao = database.cycleProfileDao()
        private val pregnancyDao = database.pregnancyRecordDao()
        private val datingDao = database.pregnancyDatingRevisionDao()
        private val dayLogDao = database.pregnancyDayLogDao()
        private val appointmentDao = database.pregnancyAppointmentDao()
        private val kickSessionDao = database.kickSessionDao()
        private val kickEventDao = database.kickEventDao()
        private val contractionSessionDao = database.contractionSessionDao()
        private val contractionEventDao = database.contractionEventDao()

        fun observeProfile(): Flow<CycleProfileEntity?> = profileDao.observeProfile()

        fun observeActivePregnancy(): Flow<PregnancyRecordEntity?> = pregnancyDao.observeActive()

        fun observePregnancyDayLogs(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<PregnancyDayLogEntity>> = dayLogDao.observeRange(start, end)

        fun observeAppointments(pregnancyId: String): Flow<List<PregnancyAppointmentEntity>> =
            appointmentDao.observeForPregnancy(pregnancyId)

        fun observeKickSessions(pregnancyId: String): Flow<List<KickSessionEntity>> =
            kickSessionDao.observeForPregnancy(pregnancyId)

        fun observeContractionSessions(pregnancyId: String): Flow<List<ContractionSessionEntity>> =
            contractionSessionDao.observeForPregnancy(pregnancyId)

        suspend fun getActivePregnancy(): PregnancyRecordEntity? = pregnancyDao.getActive()

        suspend fun getPregnancyDayLogForDate(date: LocalDate): PregnancyDayLogEntity? = dayLogDao.getForDate(date)

        suspend fun getReproductiveMode(): ReproductiveMode =
            profileDao.getProfile()?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING

        suspend fun upcomingAppointments(
            pregnancyId: String,
            fromEpochMs: Long,
            limit: Int = 5,
        ): List<PregnancyAppointmentEntity> = appointmentDao.upcoming(pregnancyId, fromEpochMs, limit)

        suspend fun shouldSuppressCelebratoryNotifications(): Boolean {
            val active = pregnancyDao.getActive()
            if (active?.suppressCelebratoryNotifications == true) return true
            return pregnancyDao.getAll().any { record ->
                record.outcome == PregnancyOutcome.PREGNANCY_LOSS ||
                    record.outcome == PregnancyOutcome.TERMINATION
            }
        }

        suspend fun startPregnancy(
            lmpDate: LocalDate,
            clinicalEddDate: LocalDate?,
            datingReason: String?,
        ): PregnancyRecordEntity {
            timerPreferences.clearAllActiveTimers()
            val now = System.currentTimeMillis()
            val dating = PregnancyDatingEngineV1.resolveDating(lmpDate, clinicalEddDate)
            val id = UUID.randomUUID().toString()
            val entity =
                PregnancyRecordEntity(
                    id = id,
                    lmpDate = lmpDate,
                    clinicalEddDate = clinicalEddDate,
                    effectiveEddDate = dating.effectiveEddDate,
                    datingSource = dating.datingSource,
                    isActive = true,
                    outcome = null,
                    outcomeRecordedAtEpochMs = null,
                    suppressCelebratoryNotifications = false,
                    wantsSupportContent = null,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                )
            pregnancyDao.getAll().filter { it.isActive }.forEach { prior ->
                pregnancyDao.upsert(prior.copy(isActive = false, updatedAtEpochMs = now))
            }
            pregnancyDao.upsert(entity)
            datingDao.insert(
                PregnancyDatingRevisionEntity(
                    id = UUID.randomUUID().toString(),
                    pregnancyId = id,
                    effectiveEddDate = dating.effectiveEddDate,
                    datingSource = dating.datingSource,
                    reason = datingReason,
                    changedAtEpochMs = now,
                ),
            )
            updateReproductiveMode(ReproductiveMode.PREGNANT)
            return entity
        }

        suspend fun updateClinicalDating(
            pregnancyId: String,
            clinicalEddDate: LocalDate?,
            reason: String?,
        ) {
            val existing = pregnancyDao.getActive() ?: return
            if (existing.id != pregnancyId) return
            val now = System.currentTimeMillis()
            val dating = PregnancyDatingEngineV1.resolveDating(existing.lmpDate, clinicalEddDate)
            pregnancyDao.upsert(
                existing.copy(
                    clinicalEddDate = clinicalEddDate,
                    effectiveEddDate = dating.effectiveEddDate,
                    datingSource = dating.datingSource,
                    updatedAtEpochMs = now,
                ),
            )
            datingDao.insert(
                PregnancyDatingRevisionEntity(
                    id = UUID.randomUUID().toString(),
                    pregnancyId = pregnancyId,
                    effectiveEddDate = dating.effectiveEddDate,
                    datingSource = dating.datingSource,
                    reason = reason,
                    changedAtEpochMs = now,
                ),
            )
        }

        suspend fun recordOutcome(
            pregnancyId: String,
            outcome: PregnancyOutcome,
            wantsSupportContent: Boolean?,
        ) {
            val existing = pregnancyDao.getActive() ?: return
            if (existing.id != pregnancyId) return
            val now = System.currentTimeMillis()
            val suppress =
                outcome == PregnancyOutcome.PREGNANCY_LOSS ||
                    outcome == PregnancyOutcome.TERMINATION
            pregnancyDao.upsert(
                existing.copy(
                    isActive = false,
                    outcome = outcome,
                    outcomeRecordedAtEpochMs = now,
                    suppressCelebratoryNotifications = suppress || existing.suppressCelebratoryNotifications,
                    wantsSupportContent = wantsSupportContent,
                    updatedAtEpochMs = now,
                ),
            )
            updateReproductiveMode(ReproductiveMode.POST_PREGNANCY_TRANSITION)
            timerPreferences.clearAllActiveTimers()
        }

        suspend fun postTransitionLearnLinkVisible(): Boolean {
            val latest =
                pregnancyDao
                    .getAll()
                    .filter { it.outcome != null }
                    .maxByOrNull { it.outcomeRecordedAtEpochMs ?: 0L }
            return latest?.wantsSupportContent == true
        }

        suspend fun resumeTracking(mode: ReproductiveMode) {
            require(mode == ReproductiveMode.CYCLE_TRACKING || mode == ReproductiveMode.TRYING_TO_CONCEIVE)
            updateReproductiveMode(mode)
        }

        suspend fun upsertPregnancyDayLog(input: PregnancyDayLogInput) {
            val existing = dayLogDao.getForDate(input.logDate)
            val hasAny =
                input.symptomTags.isNotEmpty() ||
                    input.weightKg != null ||
                    input.bpSystolic != null ||
                    input.note?.isNotBlank() == true
            if (!hasAny) {
                if (existing != null) {
                    dayLogDao.deleteByDate(input.logDate)
                }
                return
            }
            val id = existing?.id ?: UUID.randomUUID().toString()
            dayLogDao.upsert(
                PregnancyDayLogEntity(
                    id = id,
                    logDate = input.logDate,
                    symptomTags = input.symptomTags.joinToString(","),
                    weightKg = input.weightKg,
                    bpSystolic = input.bpSystolic,
                    bpDiastolic = input.bpDiastolic,
                    note = input.note?.takeIf { it.isNotBlank() },
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun upsertAppointment(input: PregnancyAppointmentInput) {
            val id = input.id ?: UUID.randomUUID().toString()
            appointmentDao.upsert(
                PregnancyAppointmentEntity(
                    id = id,
                    pregnancyId = input.pregnancyId,
                    appointmentType = input.appointmentType,
                    title = input.title,
                    scheduledAtEpochMs = input.scheduledAtEpochMs,
                    location = input.location?.takeIf { it.isNotBlank() },
                    clinicianName = input.clinicianName?.takeIf { it.isNotBlank() },
                    note = input.note?.takeIf { it.isNotBlank() },
                    reminderEnabled = input.reminderEnabled,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun deleteAppointment(id: String) {
            appointmentDao.deleteById(id)
        }

        suspend fun startKickSession(pregnancyId: String): KickSessionEntity {
            val now = System.currentTimeMillis()
            val session =
                KickSessionEntity(
                    id = UUID.randomUUID().toString(),
                    pregnancyId = pregnancyId,
                    startedAtEpochMs = now,
                    endedAtEpochMs = null,
                    updatedAtEpochMs = now,
                )
            kickSessionDao.upsert(session)
            return session
        }

        suspend fun endKickSession(sessionId: String) {
            val session = kickSessionDao.getById(sessionId) ?: return
            val now = System.currentTimeMillis()
            kickSessionDao.upsert(session.copy(endedAtEpochMs = now, updatedAtEpochMs = now))
        }

        suspend fun recordKick(sessionId: String) {
            kickEventDao.insert(
                KickEventEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    recordedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun kickCount(sessionId: String): Int = kickEventDao.countForSession(sessionId)

        suspend fun findKickSession(sessionId: String): KickSessionEntity? = kickSessionDao.getById(sessionId)

        suspend fun findContractionSession(sessionId: String): ContractionSessionEntity? =
            contractionSessionDao.getById(sessionId)

        suspend fun validateKickTimerSession(
            sessionId: String?,
            activePregnancyId: String?,
        ): String? {
            if (sessionId == null) return null
            val valid =
                activePregnancyId != null &&
                    kickSessionDao.getById(sessionId)?.pregnancyId == activePregnancyId
            if (!valid) {
                timerPreferences.setActiveKickSession(null, null)
                return null
            }
            return sessionId
        }

        suspend fun validateContractionTimerSession(
            sessionId: String?,
            openEventId: String?,
            activePregnancyId: String?,
        ): Pair<String?, String?> {
            if (sessionId == null) return null to null
            val valid =
                activePregnancyId != null &&
                    contractionSessionDao.getById(sessionId)?.pregnancyId == activePregnancyId
            if (!valid) {
                timerPreferences.setActiveContractionTimer(null, null, null)
                return null to null
            }
            return sessionId to openEventId
        }

        suspend fun startContractionSession(pregnancyId: String): ContractionSessionEntity {
            val now = System.currentTimeMillis()
            val session =
                ContractionSessionEntity(
                    id = UUID.randomUUID().toString(),
                    pregnancyId = pregnancyId,
                    startedAtEpochMs = now,
                    endedAtEpochMs = null,
                    updatedAtEpochMs = now,
                )
            contractionSessionDao.upsert(session)
            return session
        }

        suspend fun endContractionSession(sessionId: String) {
            val session = contractionSessionDao.getById(sessionId) ?: return
            val now = System.currentTimeMillis()
            contractionSessionDao.upsert(session.copy(endedAtEpochMs = now, updatedAtEpochMs = now))
        }

        suspend fun startContraction(sessionId: String): ContractionEventEntity {
            val event =
                ContractionEventEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    startedAtEpochMs = System.currentTimeMillis(),
                    endedAtEpochMs = null,
                )
            contractionEventDao.insert(event)
            return event
        }

        suspend fun stopContraction(eventId: String) {
            val event = contractionEventDao.getById(eventId) ?: return
            if (event.endedAtEpochMs != null) return
            contractionEventDao.upsert(
                event.copy(endedAtEpochMs = System.currentTimeMillis()),
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
