package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.FakeContentApi
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.MediaAssetMemoryCache
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.notifications.ReminderCoordinator
import dev.mahin.core.notifications.ReminderScheduler
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.domain.reminders.PlannedReminder
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyAppointmentsViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyTrackingRepository
    private val viewModelStore = ViewModelStoreTestHarness()
    private var viewModel: PregnancyAppointmentsViewModel? = null

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.openHelper.writableDatabase
        repository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        viewModel = null
        viewModelStore.clear()
        ShadowLooper.idleMainLooper()
        database.close()
    }

    @Test
    fun addAppointment_persistsForActivePregnancy() {
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
            repository.startPregnancy(
                lmpDate = LocalDate.of(2025, 3, 1),
                clinicalEddDate = null,
                datingReason = null,
            )
            val vm = createViewModel()
            awaitUntil { vm.uiState.value.hasActivePregnancy }
            vm.onNewAppointmentTitleChange("ویزیت")
            vm.onNewAppointmentTypeSelected(PregnancyAppointmentType.CLINICIAN_VISIT)
            vm.addAppointment()
            awaitUntil { vm.uiState.value.appointments.size == 1 }
            assertThat(
                vm.uiState.value.appointments
                    .first()
                    .title,
            ).isEqualTo("ویزیت")
        }
    }

    private fun createViewModel(): PregnancyAppointmentsViewModel =
        viewModelStore
            .hold(
                PregnancyAppointmentsViewModel(
                    repository = repository,
                    contentRepository = contentRepository(),
                    notificationPreferencesRepository =
                        NotificationPreferencesRepository(
                            ApplicationProvider.getApplicationContext(),
                        ),
                    reminderCoordinator = ReminderCoordinator(NoOpReminderScheduler()),
                    localDateClock = FixedLocalDateClock(LocalDate.of(2025, 4, 1)),
                ),
            ).also { viewModel = it }

    private suspend fun awaitUntil(
        timeoutMs: Long = 2_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
            delay(25)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }

    private fun contentRepository(): ContentRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ContentRepository(
            FakeContentApi(),
            ContentCachePreferencesRepository(context),
            WellnessContentLocalStore(database.wellnessContentDao(), Json { ignoreUnknownKeys = true }),
            Json { ignoreUnknownKeys = true },
            MediaAssetMemoryCache(),
        )
    }

    private class NoOpReminderScheduler : ReminderScheduler {
        override suspend fun applyPlans(plans: List<PlannedReminder>) = Unit

        override suspend fun cancelAllMahinReminders() = Unit

        override fun requestFullRefresh() = Unit
    }
}
