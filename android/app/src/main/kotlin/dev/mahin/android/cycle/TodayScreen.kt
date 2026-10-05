package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.assistant.AssistantSettingsScreen
import dev.mahin.android.healthconnect.HealthConnectSettingsScreen
import dev.mahin.android.notifications.NotificationSettingsScreen
import dev.mahin.android.pregnancy.PregnancyStartSheet
import dev.mahin.android.pregnancy.PregnancyTodayCard
import dev.mahin.android.privacy.PrivacySecuritySettingsScreen
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSettingsEntry
import dev.mahin.core.designsystem.component.MahinSettingsGroup
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PredictionConfidence

@Composable
@Suppress("LongMethod")
fun TodayScreen(
    modifier: Modifier = Modifier,
    onOpenDataExport: () -> Unit = {},
    onLocalDataErased: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotificationSettings by rememberSaveable { mutableStateOf(false) }
    var showPrivacySettings by rememberSaveable { mutableStateOf(false) }
    var showHealthConnectSettings by rememberSaveable { mutableStateOf(false) }
    var showAssistantSettings by rememberSaveable { mutableStateOf(false) }
    if (showNotificationSettings) {
        NotificationSettingsScreen(onNavigateUp = { showNotificationSettings = false })
        return
    }
    if (showPrivacySettings) {
        PrivacySecuritySettingsScreen(
            onNavigateUp = { showPrivacySettings = false },
            onLocalDataErased = {
                showPrivacySettings = false
                onLocalDataErased()
            },
        )
        return
    }
    if (showHealthConnectSettings) {
        HealthConnectSettingsScreen(onNavigateUp = { showHealthConnectSettings = false })
        return
    }
    if (showAssistantSettings) {
        AssistantSettingsScreen(onNavigateUp = { showAssistantSettings = false })
        return
    }
    val settingsEntries =
        todaySettingsEntries(
            healthConnectVisible = state.healthConnectEntryVisible,
            healthAssistantVisible = state.healthAssistantEntryVisible,
            callbacks =
                TodaySettingsCallbacks(
                    onOpenNotifications = { showNotificationSettings = true },
                    onOpenPrivacy = { showPrivacySettings = true },
                    onOpenHealthConnect = { showHealthConnectSettings = true },
                    onOpenAssistant = { showAssistantSettings = true },
                    onOpenDataExport = onOpenDataExport,
                ),
        )
    PregnancyStartSheet(
        visible = state.showPregnancyStartSheet,
        onDismiss = viewModel::dismissPregnancyStartSheet,
        onConfirm = viewModel::confirmPregnancyStart,
    )
    TodayScreenContent(
        state = state,
        settingsEntries = settingsEntries,
        onModeSelected = viewModel::onReproductiveModeSelected,
        modifier = modifier,
    )
}

@Composable
@Suppress("LongMethod")
internal fun TodayScreenContent(
    state: TodayUiState,
    settingsEntries: List<MahinSettingsEntry>,
    onModeSelected: (ReproductiveMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("today_screen_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            MahinScreenHeader(
                title = stringResource(R.string.today_title),
                subtitle = stringResource(R.string.today_subtitle),
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
        val dashboard = state.dashboard
        if (dashboard == null) {
            item {
                MahinEmptyState(
                    title = stringResource(R.string.today_empty_title),
                    body = stringResource(R.string.today_empty_body),
                )
            }
        } else if (state.reproductiveMode == ReproductiveMode.PREGNANT) {
            val pregnancyStatus = state.pregnancyStatus
            if (pregnancyStatus != null) {
                item {
                    PregnancyTodayCard(
                        status = pregnancyStatus,
                        modifier = Modifier.padding(vertical = MahinSpacing.sm),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.pregnancy_mode_active_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            item {
                TodayPredictionCard(prediction = dashboard.prediction)
            }
            if (dashboard.onPeriodToday || dashboard.todayLog != null) {
                item {
                    TodayStatusRow(
                        onPeriodToday = dashboard.onPeriodToday,
                        hasDailyLog = dashboard.todayLog != null,
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(MahinSpacing.lg))
        }
        item {
            MahinSettingsGroup(
                title = stringResource(R.string.today_settings_heading),
                entries = settingsEntries,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Spacer(modifier = Modifier.height(MahinSpacing.md))
        }
        item {
            ReproductiveModeCard(
                currentMode = state.reproductiveMode,
                hasActivePregnancy = state.hasActivePregnancy,
                onModeSelected = onModeSelected,
            )
        }
        if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            item {
                Text(
                    text = stringResource(R.string.today_ttc_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
            }
        }
    }
}

private data class TodaySettingsCallbacks(
    val onOpenNotifications: () -> Unit,
    val onOpenPrivacy: () -> Unit,
    val onOpenHealthConnect: () -> Unit,
    val onOpenAssistant: () -> Unit,
    val onOpenDataExport: () -> Unit,
)

@Composable
private fun todaySettingsEntries(
    healthConnectVisible: Boolean,
    healthAssistantVisible: Boolean,
    callbacks: TodaySettingsCallbacks,
): List<MahinSettingsEntry> =
    buildList {
        add(
            MahinSettingsEntry(
                label = stringResource(R.string.notification_settings_entry),
                onClick = callbacks.onOpenNotifications,
            ),
        )
        add(
            MahinSettingsEntry(
                label = stringResource(R.string.privacy_security_entry),
                onClick = callbacks.onOpenPrivacy,
            ),
        )
        if (healthConnectVisible) {
            add(
                MahinSettingsEntry(
                    label = stringResource(R.string.health_connect_entry),
                    onClick = callbacks.onOpenHealthConnect,
                ),
            )
        }
        if (healthAssistantVisible) {
            add(
                MahinSettingsEntry(
                    label = stringResource(R.string.assistant_settings_entry),
                    onClick = callbacks.onOpenAssistant,
                ),
            )
        }
        add(
            MahinSettingsEntry(
                label = stringResource(R.string.export_entry),
                onClick = callbacks.onOpenDataExport,
            ),
        )
    }

@Composable
private fun TodayStatusRow(
    onPeriodToday: Boolean,
    hasDailyLog: Boolean,
) {
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        if (onPeriodToday) {
            Text(
                text = stringResource(R.string.today_on_period),
                style = mahinTextStyle(MahinTypographyRole.Label),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (hasDailyLog) {
            val topPad = if (onPeriodToday) Modifier.padding(top = MahinSpacing.xs) else Modifier
            Text(
                text = stringResource(R.string.today_has_daily_log),
                style = MaterialTheme.typography.bodyMedium,
                modifier = topPad,
            )
        }
    }
}

@Composable
private fun TodayPredictionCard(prediction: CyclePredictionResult) {
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        prediction.cycleDay?.let { day ->
            Text(
                text = stringResource(R.string.today_cycle_day, day),
                style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
            )
        }
        Text(
            text = stringResource(R.string.today_confidence, confidenceLabel(prediction.confidence)),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (prediction.confidence == PredictionConfidence.INSUFFICIENT_DATA) {
            Text(
                text = stringResource(R.string.today_insufficient_data_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        prediction.nextPeriod?.let { range ->
            Spacer(modifier = Modifier.height(MahinSpacing.sm))
            Text(
                text = stringResource(R.string.today_next_period, CycleFormatters.formatRange(range)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        prediction.fertileWindow?.let { range ->
            Text(
                text = stringResource(R.string.today_fertile_window, CycleFormatters.formatRange(range)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(R.string.prediction_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
    }
}
