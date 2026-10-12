package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import java.util.UUID
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
class CalendarViewModelPeriodManagementTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: CycleTrackingRepository
    private lateinit var calendarPrefs: CalendarUiPreferencesRepository
    private val viewModelStore = ViewModelStoreTestHarness()
    private val converter = PersianCivilDateConverter
    private val today = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.openHelper.writableDatabase
        repository = CycleTrackingRepository(database)
        calendarPrefs = CalendarUiPreferencesRepository(context)
        runBlocking { seedProfile() }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        runBlocking { calendarPrefs.clear() }
        database.close()
    }

    @Test
    fun savePeriodEdit_overlappingRange_setsFeedback() {
        runBlocking {
            val first = insertPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5))
            insertPeriod(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 5))
            val vm = createViewModel()
            awaitUntil {
                vm.uiState.value.periodRecords
                    .isNotEmpty()
            }
            vm.beginPeriodEdit(first)
            vm.onPeriodEditStartSelected(converter.toJalali(LocalDate.of(2025, 1, 10)))
            vm.onPeriodEditEndSelected(converter.toJalali(LocalDate.of(2025, 2, 3)))
            vm.savePeriodEdit(first)
            awaitUntil { vm.uiState.value.periodFeedbackMessage == "overlap" }
        }
    }

    @Test
    fun mergePeriodWithNext_nonAdjacent_setsFeedback() {
        runBlocking {
            val first = insertPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5))
            insertPeriod(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 12))
            val vm = createViewModel()
            awaitUntil { vm.uiState.value.periodRecords.size >= 2 }
            vm.mergePeriodWithNext(first)
            awaitUntil { vm.uiState.value.periodFeedbackMessage == "merge_not_adjacent" }
        }
    }

    @Test
    fun confirmPeriodSplit_createsTwoRecords() {
        runBlocking {
            val id = insertPeriod(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 7))
            val vm = createViewModel()
            awaitUntil {
                vm.uiState.value.periodRecords
                    .any { it.id == id }
            }
            vm.beginPeriodSplit(id)
            val splitAfter = LocalDate.of(2025, 3, 4)
            vm.onPeriodSplitAfterSelected(converter.toJalali(splitAfter))
            vm.confirmPeriodSplit()
            awaitUntil { runBlocking { repository.getAllPeriods().size == 2 } }
            val periods = runBlocking { repository.getAllPeriods().sortedBy { it.startDate } }
            assertThat(periods[0].endDate).isEqualTo(splitAfter)
            assertThat(periods[1].startDate).isEqualTo(splitAfter.plusDays(1))
        }
    }

    @Test
    fun savePeriodEdit_openEndDate_persists() {
        runBlocking {
            val id = insertPeriod(LocalDate.of(2025, 4, 1), null)
            val vm = createViewModel()
            awaitUntil {
                vm.uiState.value.periodRecords
                    .any { it.id == id }
            }
            vm.beginPeriodEdit(id)
            vm.onPeriodEditEndSelected(converter.toJalali(LocalDate.of(2025, 4, 5)))
            vm.savePeriodEdit(id)
            awaitUntil { vm.uiState.value.periodEditId == null }
            val updated = repository.getAllPeriods().single { it.id == id }
            assertThat(updated.endDate).isEqualTo(LocalDate.of(2025, 4, 5))
        }
    }

    private suspend fun insertPeriod(
        start: LocalDate,
        end: LocalDate?,
    ): String {
        val id = UUID.randomUUID().toString()
        repository.upsertPeriod(startDate = start, endDate = end, note = null, recordId = id)
        return id
    }

    private suspend fun seedProfile() {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.REGULAR,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    private fun createViewModel(): CalendarViewModel =
        viewModelStore.hold(
            CalendarViewModel(
                repository = repository,
                calendarUiPreferencesRepository = calendarPrefs,
                localDateClock = FixedLocalDateClock(today),
            ),
        )

    private suspend fun awaitUntil(
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
