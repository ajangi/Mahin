package dev.mahin.android.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.ReproductiveModeCard
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSettingsEntry
import dev.mahin.core.designsystem.component.MahinSettingsGroup
import dev.mahin.core.designsystem.component.MahinShellSecondaryTopAppBar

@Composable
internal fun SettingsScreenContent(
    state: SettingsUiState,
    callbacks: SettingsScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MahinShellSecondaryTopAppBar(
                title = stringResource(R.string.settings_title),
                onNavigateUp = callbacks.onNavigateUp,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = MahinSpacing.md)
                    .testTag("settings_screen_list"),
        ) {
            settingsHeaderAndMode(state, callbacks)
            settingsGroups(state, callbacks)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsHeaderAndMode(
    state: SettingsUiState,
    callbacks: SettingsScreenCallbacks,
) {
    item {
        MahinScreenHeader(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
        )
    }
    if (state.modeChangeBlockedMessage) {
        item {
            Text(
                text = stringResource(R.string.pregnancy_active_block_mode_change),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
    item {
        ReproductiveModeCard(
            currentMode = state.reproductiveMode,
            hasActivePregnancy = state.hasActivePregnancy,
            onModeSelected = callbacks.onModeSelected,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MahinSpacing.md),
        )
    }
    if (state.postPregnancyTransition) {
        item {
            PostPregnancyResumeSection(
                onResumeCycle = callbacks.onResumeCycle,
                onResumeTtc = callbacks.onResumeTtc,
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
    }
    item { Spacer(modifier = Modifier.height(MahinSpacing.lg)) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsGroups(
    state: SettingsUiState,
    callbacks: SettingsScreenCallbacks,
) {
    settingsCoreGroups(callbacks)
    settingsOptionalGroups(state, callbacks)
    item {
        MahinSettingsGroup(
            title = stringResource(R.string.settings_group_about),
            entries =
                listOf(
                    MahinSettingsEntry(
                        label = stringResource(R.string.settings_about_app),
                        onClick = {},
                    ),
                ),
            modifier = Modifier.padding(top = MahinSpacing.md, bottom = MahinSpacing.lg),
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsCoreGroups(callbacks: SettingsScreenCallbacks) {
    item {
        MahinSettingsGroup(
            title = stringResource(R.string.settings_group_privacy),
            entries =
                listOf(
                    MahinSettingsEntry(
                        label = stringResource(R.string.privacy_security_entry),
                        onClick = callbacks.onOpenPrivacy,
                    ),
                ),
        )
    }
    item {
        MahinSettingsGroup(
            title = stringResource(R.string.settings_group_notifications),
            entries =
                listOf(
                    MahinSettingsEntry(
                        label = stringResource(R.string.notification_settings_entry),
                        onClick = callbacks.onOpenNotifications,
                    ),
                ),
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
    item {
        MahinSettingsGroup(
            title = stringResource(R.string.settings_group_your_data),
            entries =
                listOf(
                    MahinSettingsEntry(
                        label = stringResource(R.string.settings_history_entry),
                        onClick = callbacks.onOpenHistory,
                    ),
                    MahinSettingsEntry(
                        label = stringResource(R.string.export_entry),
                        onClick = callbacks.onOpenDataExport,
                    ),
                ),
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsOptionalGroups(
    state: SettingsUiState,
    callbacks: SettingsScreenCallbacks,
) {
    if (state.healthConnectEntryVisible) {
        item {
            MahinSettingsGroup(
                title = stringResource(R.string.settings_group_integrations),
                entries =
                    listOf(
                        MahinSettingsEntry(
                            label = stringResource(R.string.health_connect_entry),
                            onClick = callbacks.onOpenHealthConnect,
                        ),
                    ),
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
    }
    if (state.healthAssistantEntryVisible) {
        item {
            MahinSettingsGroup(
                title = stringResource(R.string.settings_group_assistant),
                entries =
                    listOf(
                        MahinSettingsEntry(
                            label = stringResource(R.string.assistant_settings_entry),
                            onClick = callbacks.onOpenAssistant,
                        ),
                    ),
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
    }
}

@Composable
private fun PostPregnancyResumeSection(
    onResumeCycle: () -> Unit,
    onResumeTtc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.pregnancy_post_transition_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_cycle),
            onClick = onResumeCycle,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_ttc),
            onClick = onResumeTtc,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
    }
}
