package dev.mahin.android.assistant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantSettingsScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AssistantSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (!state.launchFlagLoading && !state.launchFlagEnabled) {
        onNavigateUp()
        return
    }
    Scaffold(
        modifier = modifier.testTag("assistant_settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.assistant_settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(stringResource(R.string.privacy_back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.launchFlagLoading) {
            Text(
                text = stringResource(R.string.assistant_loading),
                modifier = Modifier.padding(padding).padding(MahinSpacing.md),
            )
            return@Scaffold
        }
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(MahinSpacing.md),
        ) {
            item {
                Text(
                    text = stringResource(R.string.assistant_consent_heading),
                    style = mahinTextStyle(MahinTypographyRole.TitleLarge),
                )
                Text(
                    text = stringResource(R.string.assistant_consent_body),
                    modifier = Modifier.padding(vertical = MahinSpacing.sm),
                )
            }
            item {
                ConsentRow(
                    label = stringResource(R.string.assistant_consent_cycle_summary),
                    checked = state.shareCycleSummary,
                    onCheckedChange = viewModel::setShareCycleSummary,
                    testTag = "assistant_consent_cycle_summary",
                )
            }
            item {
                ConsentRow(
                    label = stringResource(R.string.assistant_consent_symptom_tags),
                    checked = state.shareSymptomTags,
                    onCheckedChange = viewModel::setShareSymptomTags,
                    testTag = "assistant_consent_symptom_tags",
                )
            }
            item {
                TextButton(
                    onClick = viewModel::saveConsent,
                    enabled = !state.saving,
                    modifier = Modifier.padding(top = MahinSpacing.md),
                ) {
                    Text(stringResource(R.string.assistant_consent_save))
                }
            }
            item {
                Text(
                    text = stringResource(R.string.assistant_not_medical_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = MahinSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun ConsentRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    Column(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        Text(text = label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier =
                Modifier
                    .testTag(testTag)
                    .semantics { contentDescription = label },
        )
    }
}
