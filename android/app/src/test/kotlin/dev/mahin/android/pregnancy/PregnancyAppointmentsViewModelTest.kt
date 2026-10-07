package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.notifications.ReminderCoordinator
import dev.mahin.core.notifications.ReminderScheduler
import dev.mahin.domain.reminders.PlannedReminder
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
class PregnancyAppointmentsViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyTrackingRepository
    private lateinit var viewModel: PregnancyAppointmentsViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        viewModel =
            PregnancyAppointmentsViewModel(
                repository = repository,
                notificationPreferencesRepository = NotificationPreferencesRepository(context),
                reminderCoordinator = ReminderCoordinator(NoOpReminderScheduler()),
            )
    }

    @After
    fun tearDown() {
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
            awaitUntil { viewModel.uiState.value.hasActivePregnancy }
            viewModel.onNewAppointmentTitleChange("ویزیت")
            viewModel.onNewAppointmentTypeSelected(PregnancyAppointmentType.CLINICIAN_VISIT)
            viewModel.addAppointment()
            awaitUntil { viewModel.uiState.value.appointments.size == 1 }
            val state = viewModel.uiState.value
            assertThat(state.appointments).hasSize(1)
            assertThat(state.appointments.first().title).isEqualTo("ویزیت")
            assertThat(state.newAppointmentTitle).isEmpty()
        }
    }

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

    private class NoOpReminderScheduler : ReminderScheduler {
        override suspend fun applyPlans(plans: List<PlannedReminder>) = Unit

        override suspend fun cancelAllMahinReminders() = Unit

        override fun requestFullRefresh() = Unit
    }
}
