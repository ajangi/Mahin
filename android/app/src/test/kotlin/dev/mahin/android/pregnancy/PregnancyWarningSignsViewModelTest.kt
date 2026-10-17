package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.FakeContentApi
import dev.mahin.android.learn.RecordingAnalyticsGateway
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
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.domain.pregnancy.PregnancyBpFlagLevel
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
class PregnancyWarningSignsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyTrackingRepository
    private lateinit var contentRepository: ContentRepository
    private lateinit var api: FakeContentApi
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
        repository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        api = FakeContentApi()
        contentRepository =
            ContentRepository(
                api,
                ContentCachePreferencesRepository(context),
                WellnessContentLocalStore(database.wellnessContentDao(), Json { ignoreUnknownKeys = true }),
                Json { ignoreUnknownKeys = true },
                MediaAssetMemoryCache(),
            )
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
    }

    @Test
    fun elevatedFlag_mapsElevatedContactCopy() =
        runTest(testDispatcher) {
            seedCardAndPregnancy(bpSystolic = 145, bpDiastolic = 92)
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.bpFlagLevel).isEqualTo(PregnancyBpFlagLevel.ELEVATED)
            assertThat(state.bpFlagMessage).isEqualTo("تماس معمولی")
        }

    @Test
    fun urgentFlag_mapsUrgentContactCopy() =
        runTest(testDispatcher) {
            seedCardAndPregnancy(bpSystolic = 165, bpDiastolic = 112)
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.bpFlagLevel).isEqualTo(PregnancyBpFlagLevel.URGENT)
            assertThat(state.bpFlagMessage).isEqualTo("تماس فوری")
        }

    @Test
    fun noBpReading_keepsNoneFlag() =
        runTest(testDispatcher) {
            seedCardAndPregnancy(bpSystolic = null, bpDiastolic = null)
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.bpFlagLevel).isEqualTo(PregnancyBpFlagLevel.NONE)
            assertThat(state.bpFlagMessage).isNull()
        }

    @Test
    fun noActivePregnancy_keepsNoneFlag() =
        runTest(testDispatcher) {
            wireWarningSignsApi()
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.bpFlagLevel).isEqualTo(PregnancyBpFlagLevel.NONE)
        }

    @Test
    fun offlineWithCachedCard_showsContentAndCachedNotice() =
        runTest(testDispatcher) {
            seedCardAndPregnancy(bpSystolic = 120, bpDiastolic = 80)
            createViewModel().uiState.filter { !it.isLoading }.first()
            api.throwOnWarningSigns = true
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.failSafeNoCard).isFalse()
            assertThat(state.usingCachedCard).isTrue()
            assertThat(state.signs).isNotEmpty()
        }

    @Test
    fun nullCard_failSafeShowsLoggedBpWithoutFlag() =
        runTest(testDispatcher) {
            repository.startPregnancy(
                lmpDate = today.minusWeeks(20),
                clinicalEddDate = null,
                datingReason = null,
            )
            repository.upsertPregnancyDayLog(
                PregnancyDayLogInput(
                    logDate = today,
                    symptomTags = emptySet(),
                    weightKg = null,
                    bpSystolic = 138,
                    bpDiastolic = 88,
                    note = null,
                ),
            )
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.failSafeNoCard).isTrue()
            assertThat(state.bpFlagLevel).isEqualTo(PregnancyBpFlagLevel.NONE)
            assertThat(state.loggedBpLabel).isNotNull()
            assertThat(state.loggedBpLabel).contains("/")
        }

    private suspend fun seedCardAndPregnancy(
        bpSystolic: Int?,
        bpDiastolic: Int?,
    ) {
        wireWarningSignsApi()
        repository.startPregnancy(
            lmpDate = today.minusWeeks(20),
            clinicalEddDate = null,
            datingReason = null,
        )
        if (bpSystolic != null && bpDiastolic != null) {
            repository.upsertPregnancyDayLog(
                PregnancyDayLogInput(
                    logDate = today,
                    symptomTags = emptySet(),
                    weightKg = null,
                    bpSystolic = bpSystolic,
                    bpDiastolic = bpDiastolic,
                    note = null,
                ),
            )
        }
    }

    private fun wireWarningSignsApi() {
        val id = "warn-1"
        api.warningSignSummaries =
            listOf(
                ContentWarningSignsSummaryDto(
                    id = id,
                    slug = "warn",
                    locale = "fa-IR",
                    title = "هشدار",
                    placeholder = true,
                ),
            )
        api.warningSignsById[id] = sampleWarningSignsCard(id)
    }

    private fun sampleWarningSignsCard(id: String): ContentWarningSignsDto =
        ContentWarningSignsDto(
            id = id,
            slug = "warn",
            locale = "fa-IR",
            title = "هشدار",
            placeholder = true,
            introTitle = "مقدمه",
            introBody = "متن",
            signs = listOf(ContentWarningSignItemDto("s1", "علامت", "خلاصه")),
            escalationTitle = "ارتقا",
            escalationBody = "متن ارتقا",
            contactClinicianLabel = "تماس معمولی",
            urgentEscalationTitle = "فوری",
            urgentEscalationBody = "متن فوری",
            urgentContactClinicianLabel = "تماس فوری",
            bpThresholds =
                ContentPregnancyBpThresholdsDto(
                    elevatedSystolic = 140,
                    elevatedDiastolic = 90,
                    urgentSystolic = 160,
                    urgentDiastolic = 110,
                ),
        )

    private fun createViewModel(): PregnancyWarningSignsViewModel =
        viewModelStore.hold(
            PregnancyWarningSignsViewModel(
                contentRepository = contentRepository,
                trackingRepository = repository,
                localDateClock = FixedLocalDateClock(today),
                analytics = GuardedAnalyticsGateway(RecordingAnalyticsGateway()),
            ),
        )
}
