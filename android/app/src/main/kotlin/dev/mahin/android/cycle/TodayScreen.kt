package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.healthconnect.HealthConnectSettingsScreen
import dev.mahin.android.notifications.NotificationSettingsScreen
import dev.mahin.android.pregnancy.PregnancyStartSheet
import dev.mahin.android.pregnancy.PregnancyTodayCard
import dev.mahin.android.privacy.PrivacySecuritySettingsScreen
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
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
    PregnancyStartSheet(
        visible = state.showPregnancyStartSheet,
        onDismiss = viewModel::dismissPregnancyStartSheet,
        onConfirm = viewModel::confirmPregnancyStart,
    )
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md),
    ) {
        item {
            Text(
                text = stringResource(R.string.today_title),
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
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
            if (dashboard.onPeriodToday) {
                item {
                    Text(
                        text = stringResource(R.string.today_on_period),
                        style = mahinTextStyle(MahinTypographyRole.Label),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (dashboard.todayLog != null) {
                item {
                    Text(
                        text = stringResource(R.string.today_has_daily_log),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = MahinSpacing.sm),
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(MahinSpacing.md))
        }
        item {
            TextButton(onClick = { showNotificationSettings = true }) {
                Text(stringResource(R.string.notification_settings_entry))
            }
        }
        item {
            TextButton(onClick = { showPrivacySettings = true }) {
                Text(stringResource(R.string.privacy_security_entry))
            }
        }
        if (state.healthConnectEntryVisible) {
            item {
                TextButton(onClick = { showHealthConnectSettings = true }) {
                    Text(stringResource(R.string.health_connect_entry))
                }
            }
        }
        item {
            TextButton(onClick = onOpenDataExport) {
                Text(stringResource(R.string.export_entry))
            }
        }
        item {
            ReproductiveModeCard(
                currentMode = state.reproductiveMode,
                hasActivePregnancy = state.hasActivePregnancy,
                onModeSelected = viewModel::onReproductiveModeSelected,
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

@Composable
private fun TodayPredictionCard(prediction: CyclePredictionResult) {
    androidx.compose.material3.Card(
        colors =
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        modifier = Modifier.padding(vertical = MahinSpacing.sm),
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(MahinSpacing.md)) {
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
}
