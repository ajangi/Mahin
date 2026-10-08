package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import java.time.LocalDate
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
class CalendarViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var calendarPrefs: CalendarUiPreferencesRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cycleRepository = CycleTrackingRepository(database)
        calendarPrefs = CalendarUiPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        runBlocking { calendarPrefs.clear() }
        database.close()
    }

    @Test
    fun jumpToToday_updatesSelectedAndVisibleMonth() {
        val viewModel = CalendarViewModel(cycleRepository, calendarPrefs)
        ShadowLooper.idleMainLooper()
        viewModel.selectJalaliDate(JalaliDate(1400, 1, 1))
        viewModel.jumpToToday()
        ShadowLooper.idleMainLooper()
        val todayJalali = PersianCivilDateConverter.toJalali(LocalDate.now())
        val state = viewModel.uiState.value
        assertThat(state.selectedJalali.day).isEqualTo(todayJalali.day)
        assertThat(state.selectedJalali.month).isEqualTo(todayJalali.month)
        assertThat(state.selectedJalali.year).isEqualTo(todayJalali.year)
        assertThat(state.visibleMonth.month).isEqualTo(todayJalali.month)
        assertThat(state.visibleMonth.year).isEqualTo(todayJalali.year)
    }
}
