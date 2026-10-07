package dev.mahin.android.cycle

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.pregnancy.PostPregnancyTransitionActions
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PredictionConfidence
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class TodayScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun today_doesNotShowSettingsGroupOrModeCard() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(state = TodayUiState(reproductiveMode = ReproductiveMode.CYCLE_TRACKING))
            }
        }
        composeRule.onNodeWithText("تنظیمات و داده").assertDoesNotExist()
        composeRule.onNodeWithText("هدف ردیابی").assertDoesNotExist()
    }

    @Test
    fun today_ttcMode_showsTtcHint() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
                            dashboard =
                                dev.mahin.core.database.cycle.CycleDashboard(
                                    profile = null,
                                    prediction =
                                        CyclePredictionResult(
                                            algorithmVersion = "v1",
                                            confidence = PredictionConfidence.LOW,
                                            cycleDay = 1,
                                            nextPeriod = null,
                                            fertileWindow = null,
                                            estimatedOvulation = null,
                                            insufficientDataReason = null,
                                        ),
                                    todayLog = null,
                                    onPeriodToday = false,
                                    openPeriodStart = null,
                                ),
                        ),
                )
            }
        }
        composeRule.onNodeWithText("BBT", substring = true).assertExists()
    }

    @Test
    fun today_postPregnancyTransition_showsResumeActions() {
        var resumedCycle = false
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                        ),
                    postPregnancyActions =
                        PostPregnancyTransitionActions(
                            onResumeCycle = { resumedCycle = true },
                            onResumeTtc = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("post_pregnancy_transition_section").assertExists()
        composeRule.onNodeWithTag("post_pregnancy_resume_cycle").performClick()
        assertTrue(resumedCycle)
    }

    @Test
    fun recordOutcome_viewModelDrivesPostTransition_resumeCycleUpdatesMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val cycleRepository = CycleTrackingRepository(database)
        val pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        runBlocking {
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
            composeRule.setContent {
                MahinTheme {
                    val state by viewModel.uiState.collectAsState()
                    TodayScreenContent(
                        state = state,
                        postPregnancyActions =
                            PostPregnancyTransitionActions(
                                onResumeCycle = viewModel::resumeCycleTracking,
                                onResumeTtc = viewModel::resumeTtc,
                            ),
                    )
                }
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("post_pregnancy_transition_section").assertExists()
            composeRule.onNodeWithTag("post_pregnancy_resume_cycle").performClick()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.CYCLE_TRACKING }
        }
        database.close()
    }

    @Test
    fun recordOutcome_viewModelDrivesPostTransition_resumeTtcUpdatesMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        val cycleRepository = CycleTrackingRepository(database)
        val pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        runBlocking {
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
            composeRule.setContent {
                MahinTheme {
                    val state by viewModel.uiState.collectAsState()
                    TodayScreenContent(
                        state = state,
                        postPregnancyActions =
                            PostPregnancyTransitionActions(
                                onResumeCycle = viewModel::resumeCycleTracking,
                                onResumeTtc = viewModel::resumeTtc,
                            ),
                    )
                }
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("post_pregnancy_resume_ttc").performClick()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
        }
        database.close()
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
