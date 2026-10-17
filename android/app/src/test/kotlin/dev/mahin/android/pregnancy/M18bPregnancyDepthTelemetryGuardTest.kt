package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.FakeContentApi
import dev.mahin.android.learn.RecordingAnalyticsGateway
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.GuardedAnalyticsGateway
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentPregnancyBpThresholdsDto
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.ContentWarningSignItemDto
import dev.mahin.core.content.ContentWarningSignsDto
import dev.mahin.core.content.ContentWarningSignsSummaryDto
import dev.mahin.core.content.MediaAssetMemoryCache
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyDayLogInput
import dev.mahin.core.database.pregnancy.PregnancyExpenseRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class M18bPregnancyDepthTelemetryGuardTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var database: MahinDatabase
    private val recording = RecordingAnalyticsGateway()
    private val analytics = GuardedAnalyticsGateway(recording)
    private val viewModelStore = ViewModelStoreTestHarness()
    private val today = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
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
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
        recording.events.clear()
    }

    @Test
    fun depthScreens_emitCoarseRoutesWithoutSensitiveValues() =
        runTest(testDispatcher) {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val trackingRepository =
                PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
            trackingRepository.startPregnancy(today.minusWeeks(20), null, null)
            trackingRepository.upsertPregnancyDayLog(
                PregnancyDayLogInput(
                    logDate = today,
                    symptomTags = emptySet(),
                    weightKg = 88.4,
                    bpSystolic = 155,
                    bpDiastolic = 99,
                    note = "secret",
                ),
            )
            val api = FakeContentApi()
            val warnId = "w1"
            api.warningSignSummaries =
                listOf(
                    ContentWarningSignsSummaryDto(warnId, "w", "fa-IR", "t", placeholder = true),
                )
            api.warningSignsById[warnId] =
                ContentWarningSignsDto(
                    id = warnId,
                    slug = "w",
                    locale = "fa-IR",
                    title = "t",
                    placeholder = true,
                    introTitle = "i",
                    introBody = "b",
                    signs = listOf(ContentWarningSignItemDto("s", "t", "s")),
                    escalationTitle = "e",
                    escalationBody = "e",
                    contactClinicianLabel = "c",
                    urgentEscalationTitle = "u",
                    urgentEscalationBody = "u",
                    urgentContactClinicianLabel = "u",
                    bpThresholds =
                        ContentPregnancyBpThresholdsDto(140, 90, 160, 110),
                )
            val contentRepository =
                ContentRepository(
                    api,
                    ContentCachePreferencesRepository(context),
                    WellnessContentLocalStore(database.wellnessContentDao(), Json { ignoreUnknownKeys = true }),
                    Json { ignoreUnknownKeys = true },
                    MediaAssetMemoryCache(),
                )
            val expenseRepository = PregnancyExpenseRepository(database)

            viewModelStore
                .hold(
                    PregnancyWarningSignsViewModel(
                        contentRepository,
                        trackingRepository,
                        FixedLocalDateClock(today),
                        analytics,
                    ),
                ).uiState
                .filter { !it.isLoading }
                .first()

            val expensesVm =
                viewModelStore.hold(
                    PregnancyExpensesViewModel(
                        expenseRepository,
                        FixedLocalDateClock(today),
                        analytics,
                    ),
                )
            expensesVm.uiState.filter { !it.isLoading }.first()
            expensesVm.onDraftAmountChange("7500000")
            expensesVm.saveExpense()
            expensesVm.uiState.filter { it.expenses.isNotEmpty() }.first()

            viewModelStore
                .hold(
                    PregnancyWeightChartViewModel(
                        trackingRepository,
                        FixedLocalDateClock(today),
                        analytics,
                    ),
                ).uiState
                .filter { !it.isLoading }
                .first()

            val surfaces = recording.events.map { it.properties["surface"] }
            assertThat(surfaces).containsExactly(
                MahinTopLevelDestination.PREGNANCY_WARNING_SIGNS_ROUTE,
                MahinTopLevelDestination.PREGNANCY_EXPENSES_ROUTE,
                MahinTopLevelDestination.PREGNANCY_WEIGHT_CHART_ROUTE,
            )
            val blob = recording.events.joinToString { formatEvent(it) }
            assertThat(blob).doesNotContain("155")
            assertThat(blob).doesNotContain("99")
            assertThat(blob).doesNotContain("88.4")
            assertThat(blob).doesNotContain("7500000")
            assertThat(blob).doesNotContain("secret")
        }

    private fun formatEvent(event: AnalyticsEvent): String = "${event.name}:${event.properties}"
}
