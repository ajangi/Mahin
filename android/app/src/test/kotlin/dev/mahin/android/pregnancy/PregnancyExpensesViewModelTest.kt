package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.RecordingAnalyticsGateway
import dev.mahin.core.analytics.GuardedAnalyticsGateway
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.pregnancy.PregnancyExpenseRepository
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.domain.pregnancy.PregnancyExpenseCurrencyUnit
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyExpensesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var database: MahinDatabase
    private lateinit var expenseRepository: PregnancyExpenseRepository
    private val viewModelStore = ViewModelStoreTestHarness()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        expenseRepository = PregnancyExpenseRepository(database)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun saveExpense_validAmount_persistsRow() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            vm.uiState.filter { !it.isLoading }.first()
            vm.onDraftAmountChange("250000")
            vm.saveExpense()
            val state = vm.uiState.filter { it.expenses.isNotEmpty() }.first()
            assertThat(state.amountError).isFalse()
            assertThat(state.expenses).hasSize(1)
            assertThat(state.expenses.first().amountLabel).contains("تومان")
        }

    @Test
    fun saveExpense_zeroAmount_setsErrorFlag() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            vm.uiState.filter { !it.isLoading }.first()
            vm.onDraftAmountChange("0")
            vm.saveExpense()
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(vm.uiState.value.amountError).isTrue()
            assertThat(vm.uiState.value.expenses).isEmpty()
        }

    @Test
    fun saveExpense_tooLarge_setsErrorFlag() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            vm.uiState.filter { !it.isLoading }.first()
            vm.onDraftAmountChange("99999999999999")
            vm.saveExpense()
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(vm.uiState.value.amountError).isTrue()
        }

    @Test
    fun deleteExpense_removesRow() =
        runTest(testDispatcher) {
            expenseRepository.upsertExpense(
                id = "e1",
                category = "tests_imaging",
                amountMinor = 1000,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 6, 1),
                note = null,
            )
            val vm = createViewModel()
            vm.uiState.filter { it.expenses.isNotEmpty() }.first()
            vm.deleteExpense("e1")
            val state = vm.uiState.filter { it.expenses.isEmpty() }.first()
            assertThat(state.expenses).isEmpty()
        }

    @Test
    fun perRowCurrency_rialAndToman() =
        runTest(testDispatcher) {
            expenseRepository.upsertExpense(
                id = "t",
                category = "tests_imaging",
                amountMinor = 100,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 6, 1),
                note = null,
            )
            expenseRepository.upsertExpense(
                id = "r",
                category = "supplies",
                amountMinor = 200,
                currencyUnit = "rial",
                expenseDate = LocalDate.of(2025, 6, 2),
                note = null,
            )
            val vm = createViewModel()
            val state = vm.uiState.filter { it.expenses.size == 2 }.first()
            assertThat(state.expenses.any { it.amountLabel.contains("تومان") }).isTrue()
            assertThat(state.expenses.any { it.amountLabel.contains("ریال") }).isTrue()
            vm.onDraftCurrencyChange(PregnancyExpenseCurrencyUnit.RIAL)
            vm.onDraftAmountChange("500")
            vm.saveExpense()
            val afterSave = vm.uiState.filter { it.expenses.size == 3 }.first()
            assertThat(afterSave.expenses.any { it.amountLabel.contains("ریال") }).isTrue()
        }

    private fun createViewModel(): PregnancyExpensesViewModel =
        viewModelStore.hold(
            PregnancyExpensesViewModel(
                expenseRepository = expenseRepository,
                localDateClock = FixedLocalDateClock(LocalDate.of(2025, 6, 10)),
                analytics = GuardedAnalyticsGateway(RecordingAnalyticsGateway()),
            ),
        )
}
