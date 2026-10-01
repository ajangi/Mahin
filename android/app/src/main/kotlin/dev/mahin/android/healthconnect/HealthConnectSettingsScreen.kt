package dev.mahin.android.healthconnect

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.privacy.SensitiveScreenProtection
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.healthconnect.HealthConnectAvailability
import dev.mahin.core.healthconnect.HealthConnectPermissionPolicy
import dev.mahin.core.healthconnect.HealthConnectPermissionRequests

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun HealthConnectSettingsScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HealthConnectSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SensitiveScreenProtection(enabled = true)
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = HealthConnectPermissionRequests.createPermissionResultContract(),
        ) { granted ->
            viewModel.onPermissionsResult(granted)
        }

    if (!state.launchFlagEnabled) {
        LaunchedEffect(Unit) { onNavigateUp() }
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_connect_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(stringResource(R.string.privacy_back))
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
                    text = stringResource(R.string.health_connect_intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                AvailabilityHint(availability = state.availability)
            }
            item {
                OptInRow(
                    checked = state.userOptIn,
                    onCheckedChange = viewModel::setUserOptIn,
                )
            }
            if (state.userOptIn) {
                item {
                    Text(
                        text = stringResource(R.string.health_connect_education_heading),
                        style = mahinTextStyle(MahinTypographyRole.TitleLarge),
                        modifier = Modifier.padding(top = MahinSpacing.md),
                    )
                }
                viewModel.permissionRationale().forEach { rationale ->
                    item {
                        Column(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
                            Text(text = rationale.recordLabelFa, style = MaterialTheme.typography.titleSmall)
                            Text(text = rationale.reasonFa, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text =
                                    stringResource(
                                        R.string.health_connect_maps_to,
                                        rationale.mapsToFeatureFa,
                                    ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (!state.educationAcknowledged) {
                    item {
                        Button(onClick = viewModel::acknowledgeEducation) {
                            Text(stringResource(R.string.health_connect_education_ack))
                        }
                    }
                } else if (!state.permissionsGranted) {
                    item {
                        Button(
                            onClick = {
                                permissionLauncher.launch(HealthConnectPermissionPolicy.requiredPermissions)
                            },
                        ) {
                            Text(stringResource(R.string.health_connect_request_permissions))
                        }
                    }
                } else {
                    item {
                        Button(
                            onClick = viewModel::importNow,
                            enabled = !state.busy,
                            modifier = Modifier.padding(top = MahinSpacing.sm),
                        ) {
                            Text(stringResource(R.string.health_connect_import))
                        }
                        Button(
                            onClick = viewModel::exportNow,
                            enabled = !state.busy,
                            modifier = Modifier.padding(top = MahinSpacing.sm),
                        ) {
                            Text(stringResource(R.string.health_connect_export))
                        }
                    }
                }
            }
            state.statusMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = MahinSpacing.md),
                    )
                }
            }
            if (state.locked) {
                item {
                    Text(
                        text = stringResource(R.string.health_connect_locked),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun OptInRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.md)) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.health_connect_opt_in),
                modifier = Modifier.weight(1f),
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun AvailabilityHint(availability: HealthConnectAvailability) {
    val text =
        when (availability) {
            HealthConnectAvailability.FEATURE_DISABLED -> stringResource(R.string.health_connect_unavailable_flag)
            HealthConnectAvailability.NOT_INSTALLED -> stringResource(R.string.health_connect_not_installed)
            HealthConnectAvailability.UPDATE_REQUIRED -> stringResource(R.string.health_connect_update_required)
            HealthConnectAvailability.SDK_UNAVAILABLE -> stringResource(R.string.health_connect_sdk_unavailable)
            HealthConnectAvailability.READY -> stringResource(R.string.health_connect_ready)
        }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = MahinSpacing.sm),
    )
}
