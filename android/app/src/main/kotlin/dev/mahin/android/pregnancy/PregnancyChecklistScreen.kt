package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader

data class PregnancyChecklistItemUi(
    val id: String,
    val section: String,
    val label: String,
    val checked: Boolean,
)

data class PregnancyChecklistUiState(
    val isLoading: Boolean = true,
    val items: List<PregnancyChecklistItemUi> = emptyList(),
    val progressLabel: String = "",
)

@Composable
fun PregnancyChecklistScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyChecklistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyChecklistScreenContent(
        state = state,
        onToggle = viewModel::toggleItem,
        modifier = modifier,
    )
}

@Composable
fun PregnancyChecklistScreenContent(
    state: PregnancyChecklistUiState,
    onToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_checklist")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_checklist_title),
            subtitle = stringResource(R.string.pregnancy_checklist_subtitle),
        )
        if (!state.isLoading && state.items.isNotEmpty()) {
            Text(
                text = state.progressLabel,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = MahinSpacing.sm),
            )
        }
        when {
            state.isLoading -> Unit
            state.items.isEmpty() -> {
                MahinEmptyState(
                    title = stringResource(R.string.pregnancy_checklist_empty_title),
                    body = stringResource(R.string.pregnancy_checklist_empty_body),
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.testTag("pregnancy_checklist_list")) {
                    items(state.items, key = { it.id }) { item ->
                        ChecklistRow(item = item, onToggle = onToggle)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: PregnancyChecklistItemUi,
    onToggle: (String, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.xs)) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggle(item.id, it) },
            modifier = Modifier.testTag("pregnancy_checklist_item_${item.id}"),
        )
        Text(text = item.label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = item.section,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
