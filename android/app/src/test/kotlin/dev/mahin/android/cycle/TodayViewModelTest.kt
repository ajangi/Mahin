package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TodayViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cycleRepository = CycleTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordOutcome_exposesPostPregnancyTransition() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository)
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            assertThat(viewModel.uiState.value.reproductiveMode)
                .isEqualTo(ReproductiveMode.POST_PREGNANCY_TRANSITION)
        }
    }

    @Test
    fun resumeCycleTracking_leavesPostTransitionMode() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository)
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            viewModel.resumeCycleTracking()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.CYCLE_TRACKING }
            assertThat(viewModel.uiState.value.postPregnancyTransition).isFalse()
        }
    }

    @Test
    fun resumeTtc_leavesPostTransitionMode() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository)
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            viewModel.resumeTtc()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
            assertThat(viewModel.uiState.value.postPregnancyTransition).isFalse()
        }
    }

    @Test
    fun postTransitionLearnLinkVisible_whenSupportContentRequested() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository)
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = true,
            )
            awaitUntil { viewModel.uiState.value.postTransitionLearnLinkVisible }
        }
    }

    private suspend fun seedPregnantProfile() {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = ReproductiveMode.PREGNANT,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.UNKNOWN,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    private suspend fun awaitUntil(predicate: () -> Boolean) {
        repeat(200) {
            if (predicate()) return
            delay(10)
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met")
    }
}
