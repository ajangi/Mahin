package dev.mahin.core.database.ttc

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TtcTrackingRepositoryTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: TtcTrackingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = TtcTrackingRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateReproductiveMode_withoutProfile_isNoOp() =
        runTest {
            repository.updateReproductiveMode(ReproductiveMode.TRYING_TO_CONCEIVE)
            assertThat(database.cycleProfileDao().getProfile()).isNull()
        }

    @Test
    fun switchingMode_preservesTtcLogs() =
        runTest {
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
            val date = LocalDate.of(2025, 4, 1)
            database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "log1",
                    logDate = date,
                    bbtCelsius = 36.5,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            repository.updateReproductiveMode(ReproductiveMode.TRYING_TO_CONCEIVE)
            assertThat(database.ttcDayLogDao().getForDate(date)?.bbtCelsius).isEqualTo(36.5)
        }

    @Test
    fun upsertTtcDayLog_clearingAllFields_deletesRow() =
        runTest {
            val date = LocalDate.of(2025, 4, 2)
            database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "log2",
                    logDate = date,
                    bbtCelsius = 36.4,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            repository.upsertTtcDayLog(
                TtcDayLogInput(
                    logDate = date,
                    bbtCelsius = null,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                ),
            )
            assertThat(database.ttcDayLogDao().getForDate(date)).isNull()
        }
}
