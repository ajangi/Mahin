package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.entity.PregnancyExpenseEntity
import dev.mahin.core.database.pregnancy.PregnancyExpenseRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.domain.pregnancy.PregnancyExpenseCurrencyUnit
import dev.mahin.domain.pregnancy.PregnancyExpenseLine
import dev.mahin.domain.pregnancy.PregnancyExpenseMath
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PregnancyExpenseRowUi(
    val id: String,
    val category: String,
    val amountLabel: String,
    val dateLabel: String,
    val note: String?,
)

data class PregnancyExpenseCategorySummaryUi(
    val category: String,
    val totalLabel: String,
)

data class PregnancyExpensesUiState(
    val isLoading: Boolean = true,
    val expenses: List<PregnancyExpenseRowUi> = emptyList(),
    val categorySummaries: List<PregnancyExpenseCategorySummaryUi> = emptyList(),
    val draftCategory: String = PregnancyExpenseRepository.DEFAULT_CATEGORIES.first(),
    val draftAmount: String = "",
    val draftCurrency: PregnancyExpenseCurrencyUnit = PregnancyExpenseCurrencyUnit.TOMAN,
    val draftNote: String = "",
    val amountError: Boolean = false,
)

@HiltViewModel
class PregnancyExpensesViewModel
    @Inject
    constructor(
        private val expenseRepository: PregnancyExpenseRepository,
        private val localDateClock: LocalDateClock,
        private val analytics: AnalyticsGateway,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyExpensesUiState())
        val uiState: StateFlow<PregnancyExpensesUiState> = _uiState.asStateFlow()

        init {
            analytics.track(
                AnalyticsEvent(
                    "foundation_opened",
                    mapOf("surface" to MahinTopLevelDestination.PREGNANCY_EXPENSES_ROUTE),
                ),
            )
            viewModelScope.launch {
                expenseRepository.observeAll().collect { rows ->
                    val lines = rows.map { it.toLine() }
                    val summaries =
                        PregnancyExpenseMath.summarizeByCategory(lines).map { total ->
                            PregnancyExpenseCategorySummaryUi(
                                category = total.category,
                                totalLabel =
                                    PregnancyExpenseMath.formatAmount(
                                        total.totalMinor,
                                        total.currencyUnit,
                                    ),
                            )
                        }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expenses = rows.map { row -> row.toUi() },
                            categorySummaries = summaries,
                        )
                    }
                }
            }
        }

        fun onDraftCategoryChange(category: String) {
            _uiState.update { it.copy(draftCategory = category) }
        }

        fun onDraftAmountChange(value: String) {
            _uiState.update { it.copy(draftAmount = value, amountError = false) }
        }

        fun onDraftCurrencyChange(unit: PregnancyExpenseCurrencyUnit) {
            _uiState.update { it.copy(draftCurrency = unit) }
        }

        fun onDraftNoteChange(value: String) {
            _uiState.update { it.copy(draftNote = value) }
        }

        fun saveExpense() {
            viewModelScope.launch {
                val state = _uiState.value
                val (amount, error) = PregnancyExpenseMath.parseAmountInput(state.draftAmount)
                if (error != null || amount == null) {
                    _uiState.update { it.copy(amountError = true) }
                    return@launch
                }
                expenseRepository.upsertExpense(
                    id = null,
                    category = state.draftCategory,
                    amountMinor = amount,
                    currencyUnit = state.draftCurrency.name.lowercase(),
                    expenseDate = localDateClock.today(),
                    note = state.draftNote,
                )
                _uiState.update {
                    it.copy(draftAmount = "", draftNote = "", amountError = false)
                }
            }
        }

        fun deleteExpense(id: String) {
            viewModelScope.launch { expenseRepository.deleteExpense(id) }
        }

        private fun PregnancyExpenseEntity.toLine(): PregnancyExpenseLine =
            PregnancyExpenseLine(
                category = category,
                amountMinor = amountMinor,
                currencyUnit =
                    if (currencyUnit == "rial") {
                        PregnancyExpenseCurrencyUnit.RIAL
                    } else {
                        PregnancyExpenseCurrencyUnit.TOMAN
                    },
            )

        private fun PregnancyExpenseEntity.toUi(): PregnancyExpenseRowUi {
            val unit =
                if (currencyUnit == "rial") {
                    PregnancyExpenseCurrencyUnit.RIAL
                } else {
                    PregnancyExpenseCurrencyUnit.TOMAN
                }
            val jalali = PersianCivilDateConverter.toJalali(java.time.LocalDate.parse(expenseDate))
            return PregnancyExpenseRowUi(
                id = id,
                category = category,
                amountLabel = PregnancyExpenseMath.formatAmount(amountMinor, unit),
                dateLabel =
                    PregnancyFormatters.formatInteger(jalali.year) + "/" +
                        PregnancyFormatters.formatInteger(jalali.month) + "/" +
                        PregnancyFormatters.formatInteger(jalali.day),
                note = note,
            )
        }
    }
