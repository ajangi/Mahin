package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.cycle.editor.DayEditorCoordinator
import dev.mahin.android.cycle.editor.DayEditorSheetContent
import dev.mahin.android.cycle.editor.DayEditorViewModel
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.LoggingPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class TodayDayEditorIntegrationTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    private lateinit var harness: DayEditorTestHarness

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create()
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun quickLog_opensEditor_autosavesWithoutSaveOrDialog() {
        val today = LocalDate.of(2025, 6, 15)
        runBlocking {
            harness.seedCycleProfile()
            val coordinator = DayEditorCoordinator()
            val viewModel = harness.createViewModel(today)
            androidComposeRule.setContent {
                val session by coordinator.session.collectAsState()
                val editorState by viewModel.uiState.collectAsState()
                Box(Modifier.fillMaxSize()) {
                    TodayScreenContent(
                        state = M15GoldenFixtures.todayFertileWindow().copy(today = today),
                        actions = TodayScreenActions(onOpenLogForDate = { coordinator.open(it) }),
                    )
                    val active = session
                    if (active != null) {
                        LaunchedEffect(active.date) { viewModel.openDate(active.date) }
                        androidx.compose.foundation.layout.Column(Modifier.testTag("day_editor_sheet")) {
                            DayEditorSheetContent(
                                date = active.date,
                                state = editorState,
                                onTogglePeriod = viewModel::togglePeriodLogged,
                                onToggleSymptom = viewModel::toggleSymptom,
                                onToggleMood = viewModel::toggleMood,
                                onSetPain = viewModel::setPain,
                                onAdjustPainSeverity = viewModel::adjustPainSeverity,
                                onOpenManage = viewModel::openManageCategories,
                                onRequestSexualOptIn = viewModel::requestSexualOptIn,
                            )
                        }
                    }
                }
            }
            androidComposeRule.onNodeWithTag("today_screen_list").performScrollToNode(hasTestTag("today_quick_log_row"))
            androidComposeRule.onAllNodesWithTag("today_quick_log_chip")[0].performClick()
            androidComposeRule.waitForIdle()
            androidComposeRule.onNodeWithTag("day_editor_sheet").assertExists()
            viewModel.openDate(today)
            awaitUntil { viewModel.uiState.value.date == today && !viewModel.uiState.value.loading }
            androidComposeRule.onNodeWithTag("day_editor_save").assertDoesNotExist()
            androidComposeRule.onNodeWithTag("day_editor_sexual_opt_in").assertDoesNotExist()
            androidComposeRule.onNodeWithTag("day_editor_period_started").performClick()
            androidComposeRule
                .onNodeWithTag(
                    "day_editor_scroll",
                ).performScrollToNode(hasTestTag("day_editor_symptom_خستگی"))
            androidComposeRule.onNodeWithTag("day_editor_symptom_خستگی").performClick()
            androidComposeRule
                .onNodeWithTag(
                    "day_editor_scroll",
                ).performScrollToNode(hasTestTag("day_editor_mood_آرام"))
            androidComposeRule.onNodeWithTag("day_editor_mood_آرام").performClick()
            awaitUntil { "خستگی" in viewModel.uiState.value.symptomTags }
            awaitUntil { "آرام" in viewModel.uiState.value.moodTags }
            awaitUntil {
                val log = harness.dailyLogFor(today)
                log != null && log.symptomTags.contains("خستگی") && log.moodTags.contains("آرام")
            }
            assertThat(harness.periodDayFor(today)).isNotNull()
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 8_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
            androidComposeRule.waitForIdle()
            delay(25)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}

internal class DayEditorTestHarness private constructor(
    val cycleRepository: CycleTrackingRepository,
    private val ttcRepository: TtcTrackingRepository,
    private val loggingPreferencesRepository: LoggingPreferencesRepository,
    private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
    private val periodDayTrackingService: PeriodDayTrackingService,
    private val customSymptomTagRepository: CustomSymptomTagRepository,
    private val localDateClock: FixedLocalDateClock,
    private val viewModelStore: ViewModelStoreTestHarness,
    private val database: dev.mahin.core.database.MahinDatabase,
) {
    fun dailyLogFor(date: LocalDate): dev.mahin.core.database.entity.DailyLogEntity? =
        runBlocking(Dispatchers.IO) { database.dailyLogDao().getForDate(date) }

    fun periodDayFor(date: LocalDate): dev.mahin.core.database.entity.PeriodDayEntity? =
        runBlocking(Dispatchers.IO) { database.periodDayDao().getForDate(date) }

    fun createViewModel(today: LocalDate): DayEditorViewModel {
        val gate =
            object : HealthConnectPeriodDayIntegrationGate {
                override suspend fun isIntegrationActive(): Boolean = false
            }
        return viewModelStore.hold(
            DayEditorViewModel(
                cycleRepository = cycleRepository,
                ttcRepository = ttcRepository,
                pregnancyRepository = harnessPregnancyRepository(),
                loggingPreferencesRepository = loggingPreferencesRepository,
                ttcPrivacyRepository = ttcPrivacyRepository,
                periodDayTrackingService = periodDayTrackingService,
                healthConnectPeriodDayIntegrationGate = gate,
                customSymptomTagRepository = customSymptomTagRepository,
                localDateClock = localDateClock,
            ),
        )
    }

    private fun harnessPregnancyRepository(): dev.mahin.core.database.pregnancy.PregnancyTrackingRepository =
        dev.mahin.core.database.pregnancy.PregnancyTrackingRepository(
            database,
            dev.mahin.core.datastore.PregnancyTimerPreferencesRepository(
                androidx.test.core.app.ApplicationProvider
                    .getApplicationContext(),
            ),
        )

    suspend fun seedCycleProfile() {
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

    fun close() {
        viewModelStore.clear()
        database.close()
    }

    companion object {
        fun create(): DayEditorTestHarness {
            val context =
                androidx.test.core.app.ApplicationProvider
                    .getApplicationContext<android.content.Context>()
            val database =
                androidx.room.Room
                    .inMemoryDatabaseBuilder(context, dev.mahin.core.database.MahinDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            database.openHelper.writableDatabase
            val cycleRepository = CycleTrackingRepository(database)
            val ttcRepository = TtcTrackingRepository(database)
            val tombstoneRepository = HealthConnectPeriodDayTombstoneRepository(context)
            return DayEditorTestHarness(
                cycleRepository = cycleRepository,
                ttcRepository = ttcRepository,
                loggingPreferencesRepository = LoggingPreferencesRepository(context),
                ttcPrivacyRepository = TtcPrivacyPreferencesRepository(context),
                periodDayTrackingService = PeriodDayTrackingService(cycleRepository, tombstoneRepository),
                customSymptomTagRepository = CustomSymptomTagRepository(database),
                localDateClock = FixedLocalDateClock(LocalDate.of(2025, 6, 15)),
                viewModelStore = ViewModelStoreTestHarness(),
                database = database,
            )
        }
    }
}
