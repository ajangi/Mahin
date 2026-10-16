package dev.mahin.core.database.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyExpenseRepositoryTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyExpenseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = PregnancyExpenseRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertExpense_persistsAndDeletes() {
        runBlocking {
            repository.upsertExpense(
                id = "e1",
                category = "other",
                amountMinor = 1000,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 1, 2),
                note = null,
            )
            val rows = repository.observeAll().first()
            assertThat(rows).hasSize(1)
            assertThat(rows.first().amountMinor).isEqualTo(1000L)
            repository.deleteExpense("e1")
            assertThat(repository.observeAll().first()).isEmpty()
        }
    }

    @Test
    fun upsertExpense_rejectsBlankCategory() {
        runBlocking {
            repository.upsertExpense(
                id = "e2",
                category = "   ",
                amountMinor = 1,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 1, 2),
                note = null,
            )
            assertThat(repository.observeAll().first()).isEmpty()
        }
    }
}
