package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.domain.pregnancy.PregnancyBpFlagLevel

@Composable
fun PregnancyWarningSignsScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyWarningSignsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyWarningSignsScreenContent(state = state, modifier = modifier)
}

@Composable
fun PregnancyWarningSignsScreenContent(
    state: PregnancyWarningSignsUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_warning_signs")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_warning_signs_title),
            subtitle = stringResource(R.string.pregnancy_warning_signs_subtitle),
        )
        when {
            state.isLoading -> Unit
            state.failSafeNoCard -> {
                WarningSignsFailSafeCard(state = state)
            }
            else -> {
                if (state.usingCachedCard) {
                    Text(
                        text = stringResource(R.string.pregnancy_warning_signs_cached_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = MahinSpacing.sm),
                    )
                }
                WarningSignsBpBanner(state = state)
                WarningSignsIntro(state = state)
                WarningSignsList(signs = state.signs)
                WarningSignsEscalationCard(state = state)
            }
        }
    }
}

@Composable
private fun WarningSignsFailSafeCard(state: PregnancyWarningSignsUiState) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.sm)
                .testTag("pregnancy_warning_signs_fail_safe"),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_warning_signs_fail_safe_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.pregnancy_warning_signs_fail_safe_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MahinSpacing.xs),
            )
            state.loggedBpLabel?.let { label ->
                Text(
                    text = stringResource(R.string.pregnancy_warning_signs_logged_bp, label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
            }
            Text(
                text = stringResource(R.string.pregnancy_bp_flag_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun WarningSignsBpBanner(state: PregnancyWarningSignsUiState) {
    if (state.bpFlagLevel == PregnancyBpFlagLevel.NONE) return
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.sm)
                .testTag("pregnancy_warning_signs_bp_banner"),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text =
                    when (state.bpFlagLevel) {
                        PregnancyBpFlagLevel.ELEVATED ->
                            stringResource(R.string.pregnancy_bp_flag_elevated_title)
                        PregnancyBpFlagLevel.URGENT ->
                            stringResource(R.string.pregnancy_bp_flag_urgent_title)
                        PregnancyBpFlagLevel.NONE -> ""
                    },
                style = MaterialTheme.typography.titleSmall,
            )
            state.bpFlagMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(R.string.pregnancy_bp_flag_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WarningSignsIntro(state: PregnancyWarningSignsUiState) {
    state.introTitle?.let {
        Text(text = it, style = MaterialTheme.typography.titleMedium)
    }
    state.introBody?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = MahinSpacing.sm),
        )
    }
}

@Composable
private fun WarningSignsList(signs: List<PregnancyWarningSignItemUi>) {
    LazyColumn(modifier = Modifier.testTag("pregnancy_warning_signs_list")) {
        items(signs) { sign ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xs),
            ) {
                Column(modifier = Modifier.padding(MahinSpacing.md)) {
                    Text(text = sign.title, style = MaterialTheme.typography.titleSmall)
                    Text(text = sign.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun WarningSignsEscalationCard(state: PregnancyWarningSignsUiState) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = MahinSpacing.md)) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            state.escalationTitle?.let {
                Text(text = it, style = MaterialTheme.typography.titleSmall)
            }
            state.escalationBody?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            state.contactClinicianLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
            }
        }
    }
}
