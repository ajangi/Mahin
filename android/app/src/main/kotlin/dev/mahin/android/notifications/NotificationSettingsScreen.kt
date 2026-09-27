package dev.mahin.android.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.domain.reminders.ReminderCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onNotificationPermissionResult(granted)
        }
    LaunchedEffect(state.pendingPermissionCategory) {
        if (state.pendingPermissionCategory == null) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onNotificationPermissionResult(true)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings_title)) },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onNavigateUp) {
                        Text(stringResource(R.string.notification_settings_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(MahinSpacing.md),
        ) {
            item {
                Text(
                    text = stringResource(R.string.notification_privacy_heading),
                    style = mahinTextStyle(MahinTypographyRole.Label),
                )
                NotificationPrivacyMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.privacyMode == mode,
                        onClick = { viewModel.onPrivacyModeSelected(mode) },
                        label = { Text(privacyModeLabel(mode)) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = MahinSpacing.xs),
                    )
                }
                Text(
                    text = stringResource(R.string.notification_privacy_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MahinSpacing.sm, bottom = MahinSpacing.md),
                )
            }
            item {
                Text(
                    text = stringResource(R.string.notification_categories_heading),
                    style = mahinTextStyle(MahinTypographyRole.Label),
                )
            }
            items(ReminderCategory.entries) { category ->
                CategoryRow(
                    title = categoryLabel(category),
                    body = categoryBody(category),
                    checked = state.categoryEnabled[category] == true,
                    onCheckedChange = { enabled -> viewModel.onCategoryToggle(category, enabled) },
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.sm),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = MahinSpacing.xs),
        )
    }
}

@Composable
private fun privacyModeLabel(mode: NotificationPrivacyMode): String =
    when (mode) {
        NotificationPrivacyMode.DISCREET -> stringResource(R.string.notification_mode_discreet)
        NotificationPrivacyMode.DESCRIPTIVE -> stringResource(R.string.notification_mode_descriptive)
        NotificationPrivacyMode.OFF -> stringResource(R.string.notification_mode_off)
    }

@Composable
private fun categoryLabel(category: ReminderCategory): String =
    when (category) {
        ReminderCategory.PERIOD_UPCOMING -> stringResource(R.string.notification_cat_period_upcoming)
        ReminderCategory.PERIOD_LOGGING_FOLLOWUP -> stringResource(R.string.notification_cat_period_logging)
        ReminderCategory.TTC_LOGGING -> stringResource(R.string.notification_cat_ttc_logging)
        ReminderCategory.PREGNANCY_WEEKLY -> stringResource(R.string.notification_cat_pregnancy_weekly)
        ReminderCategory.APPOINTMENT -> stringResource(R.string.notification_cat_appointment)
    }

@Composable
private fun categoryBody(category: ReminderCategory): String =
    when (category) {
        ReminderCategory.PERIOD_UPCOMING -> stringResource(R.string.notification_cat_period_upcoming_body)
        ReminderCategory.PERIOD_LOGGING_FOLLOWUP -> stringResource(R.string.notification_cat_period_logging_body)
        ReminderCategory.TTC_LOGGING -> stringResource(R.string.notification_cat_ttc_logging_body)
        ReminderCategory.PREGNANCY_WEEKLY -> stringResource(R.string.notification_cat_pregnancy_weekly_body)
        ReminderCategory.APPOINTMENT -> stringResource(R.string.notification_cat_appointment_body)
    }
