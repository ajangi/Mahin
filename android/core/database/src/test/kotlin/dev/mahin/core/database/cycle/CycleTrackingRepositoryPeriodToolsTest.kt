package dev.mahin.core.database.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CycleTrackingRepositoryPeriodToolsTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: CycleTrackingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = CycleTrackingRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun mergeAdjacentPeriods_combinesEndDates() {
        runBlocking {
            val first =
                repository.upsertPeriod(
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 1, 3),
                    note = null,
                    recordId = null,
                )
            val second =
                repository.upsertPeriod(
                    startDate = LocalDate.of(2025, 1, 4),
                    endDate = LocalDate.of(2025, 1, 6),
                    note = null,
                    recordId = null,
                )
            repository.mergeAdjacentPeriods(first, second)
            val periods = repository.getAllPeriods()
            assertThat(periods).hasSize(1)
            assertThat(periods.first().endDate).isEqualTo(LocalDate.of(2025, 1, 6))
        }
    }

    @Test
    fun mergeAdjacentPeriods_openEndDate_doesNotCrash() {
        runBlocking {
            val first =
                repository.upsertPeriod(
                    startDate = LocalDate.of(2025, 2, 1),
                    endDate = null,
                    note = null,
                    recordId = null,
                )
            val second =
                repository.upsertPeriod(
                    startDate = LocalDate.of(2025, 2, 2),
                    endDate = LocalDate.of(2025, 2, 5),
                    note = null,
                    recordId = null,
                )
            repository.mergeAdjacentPeriods(first, second)
            val periods = repository.getAllPeriods()
            assertThat(periods).hasSize(1)
        }
    }
}
