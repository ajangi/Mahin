package dev.mahin.android.cycle.editor

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.model.PainLocation
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
class DayEditorPainTest {
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
    fun pain_persistsLocationAndSeverity() {
        runBlocking {
            val date = LocalDate.of(2025, 2, 2)
            repository.upsertDailyLog(
                date = date,
                moodTags = emptySet(),
                symptomTags = emptySet(),
                painSeverity = 7,
                painLocation = PainLocation.HEAD.name,
                painNote = null,
                note = null,
            )
            val stored = repository.getDailyLogForDate(date)
            assertThat(stored?.painSeverity).isEqualTo(7)
            assertThat(stored?.painLocation).isEqualTo(PainLocation.HEAD.name)
        }
    }
}
