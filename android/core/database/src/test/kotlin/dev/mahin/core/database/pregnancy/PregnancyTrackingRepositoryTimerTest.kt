package dev.mahin.core.database.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyTrackingRepositoryTimerTest {
    private lateinit var database: MahinDatabase
    private lateinit var timerPreferences: PregnancyTimerPreferencesRepository
    private lateinit var repository: PregnancyTrackingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        timerPreferences = PregnancyTimerPreferencesRepository(context)
        repository = PregnancyTrackingRepository(database, timerPreferences)
        runBlocking {
            database.cycleProfileDao().upsert(
                CycleProfileEntity(
                    reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = CycleRegularity.UNKNOWN,
                    onboardingCompleted = true,
                    updatedAtEpochMs = 0L,
                ),
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordOutcome_clearsTimerPreferences() =
        runBlocking {
            val pregnancy =
                repository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val kick = repository.startKickSession(pregnancy.id)
            timerPreferences.setActiveKickSession(kick.id, kick.startedAtEpochMs)
            repository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            val kickSnap = timerPreferences.observeKickTimer().first()
            val contractionSnap = timerPreferences.observeContractionTimer().first()
            assertThat(kickSnap.sessionId).isNull()
            assertThat(contractionSnap.sessionId).isNull()
        }

    @Test
    fun validateKickTimerSession_clearsStaleSessionFromPriorPregnancy() =
        runBlocking {
            val first =
                repository.startPregnancy(
                    lmpDate = LocalDate.of(2024, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val staleKick = repository.startKickSession(first.id)
            timerPreferences.setActiveKickSession(staleKick.id, staleKick.startedAtEpochMs)
            repository.recordOutcome(
                pregnancyId = first.id,
                outcome = PregnancyOutcome.LIVE_BIRTH,
                wantsSupportContent = null,
            )
            val second =
                repository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val validated =
                repository.validateKickTimerSession(
                    sessionId = staleKick.id,
                    activePregnancyId = second.id,
                )
            assertThat(validated).isNull()
            assertThat(timerPreferences.observeKickTimer().first().sessionId).isNull()
        }
}
