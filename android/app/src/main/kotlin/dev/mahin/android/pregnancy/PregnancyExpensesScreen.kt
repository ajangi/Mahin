package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.database.pregnancy.PregnancyExpenseRepository
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.domain.pregnancy.PregnancyExpenseCurrencyUnit

@Composable
fun PregnancyExpensesScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyExpensesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyExpensesScreenContent(
        state = state,
        onDraftCategoryChange = viewModel::onDraftCategoryChange,
        onDraftAmountChange = viewModel::onDraftAmountChange,
        onDraftCurrencyChange = viewModel::onDraftCurrencyChange,
        onDraftNoteChange = viewModel::onDraftNoteChange,
        onSaveExpense = viewModel::saveExpense,
        onDeleteExpense = viewModel::deleteExpense,
        modifier = modifier,
    )
}

@Composable
@Suppress("LongParameterList")
fun PregnancyExpensesScreenContent(
    state: PregnancyExpensesUiState,
    onDraftCategoryChange: (String) -> Unit,
    onDraftAmountChange: (String) -> Unit,
    onDraftCurrencyChange: (PregnancyExpenseCurrencyUnit) -> Unit,
    onDraftNoteChange: (String) -> Unit,
    onSaveExpense: () -> Unit,
    onDeleteExpense: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_expenses")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_expenses_title),
            subtitle = stringResource(R.string.pregnancy_expenses_subtitle),
        )
        if (state.categorySummaries.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.sm)) {
                Column(modifier = Modifier.padding(MahinSpacing.md)) {
                    Text(
                        text = stringResource(R.string.pregnancy_expenses_summary_heading),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    state.categorySummaries.forEach { summary ->
                        Text(
                            text =
                                stringResource(expenseCategoryLabelRes(summary.category)) +
                                    ": " + summary.totalLabel,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        ExpenseEntryForm(
            state = state,
            onDraftCategoryChange = onDraftCategoryChange,
            onDraftAmountChange = onDraftAmountChange,
            onDraftCurrencyChange = onDraftCurrencyChange,
            onDraftNoteChange = onDraftNoteChange,
            onSaveExpense = onSaveExpense,
        )
        when {
            state.isLoading -> Unit
            state.expenses.isEmpty() -> {
                MahinEmptyState(
                    title = stringResource(R.string.pregnancy_expenses_empty_title),
                    body = stringResource(R.string.pregnancy_expenses_empty_body),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.testTag("pregnancy_expenses_list"),
                    verticalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
                ) {
                    items(state.expenses, key = { it.id }) { row ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(MahinSpacing.md)) {
                                Text(text = row.amountLabel, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = stringResource(expenseCategoryLabelRes(row.category)),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = row.dateLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                row.note?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { onDeleteExpense(row.id) }) {
                                    Text(stringResource(R.string.pregnancy_expenses_delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun ExpenseEntryForm(
    state: PregnancyExpensesUiState,
    onDraftCategoryChange: (String) -> Unit,
    onDraftAmountChange: (String) -> Unit,
    onDraftCurrencyChange: (PregnancyExpenseCurrencyUnit) -> Unit,
    onDraftNoteChange: (String) -> Unit,
    onSaveExpense: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.sm)) {
        Text(text = stringResource(R.string.pregnancy_expenses_add_heading))
        Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs)) {
            PregnancyExpenseRepository.DEFAULT_CATEGORIES.forEach { category ->
                FilterChip(
                    selected = state.draftCategory == category,
                    onClick = { onDraftCategoryChange(category) },
                    label = { Text(stringResource(expenseCategoryLabelRes(category))) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs)) {
            FilterChip(
                selected = state.draftCurrency == PregnancyExpenseCurrencyUnit.TOMAN,
                onClick = { onDraftCurrencyChange(PregnancyExpenseCurrencyUnit.TOMAN) },
                label = { Text(stringResource(R.string.pregnancy_expenses_currency_toman)) },
            )
            FilterChip(
                selected = state.draftCurrency == PregnancyExpenseCurrencyUnit.RIAL,
                onClick = { onDraftCurrencyChange(PregnancyExpenseCurrencyUnit.RIAL) },
                label = { Text(stringResource(R.string.pregnancy_expenses_currency_rial)) },
            )
        }
        OutlinedTextField(
            value = state.draftAmount,
            onValueChange = onDraftAmountChange,
            label = { Text(stringResource(R.string.pregnancy_expenses_amount_label)) },
            isError = state.amountError,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("pregnancy_expenses_amount_field"),
        )
        OutlinedTextField(
            value = state.draftNote,
            onValueChange = onDraftNoteChange,
            label = { Text(stringResource(R.string.pregnancy_expenses_note_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_expenses_save),
            onClick = onSaveExpense,
            modifier = Modifier.fillMaxWidth().testTag("pregnancy_expenses_save_button"),
        )
    }
}

fun expenseCategoryLabelRes(category: String): Int =
    when (category) {
        "prenatal_care" -> R.string.pregnancy_expense_category_prenatal
        "tests_imaging" -> R.string.pregnancy_expense_category_tests
        "hospital_birth" -> R.string.pregnancy_expense_category_hospital
        "supplies" -> R.string.pregnancy_expense_category_supplies
        else -> R.string.pregnancy_expense_category_other
    }
