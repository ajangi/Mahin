package dev.mahin.android.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecuritySettingsScreen(
    onNavigateUp: () -> Unit,
    onLocalDataErased: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrivacySecurityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SensitiveScreenProtection(enabled = state.blockScreenshots && state.mode != AppLockMode.DISABLED)
    if (state.eraseConfirmVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissEraseConfirm,
            title = { Text(stringResource(R.string.privacy_erase_local)) },
            text = { Text(stringResource(R.string.privacy_erase_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.eraseLocalData(onLocalDataErased) }) {
                    Text(stringResource(R.string.privacy_erase_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissEraseConfirm) {
                    Text(stringResource(R.string.privacy_back))
                }
            },
        )
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_security_title)) },
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
                    text = stringResource(R.string.privacy_app_lock_heading),
                    style = mahinTextStyle(MahinTypographyRole.TitleLarge),
                )
                Text(
                    text = stringResource(R.string.privacy_app_lock_body),
                    modifier = Modifier.padding(vertical = MahinSpacing.sm),
                )
                AppLockMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.mode == mode,
                        onClick = { viewModel.setMode(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    AppLockMode.DISABLED -> stringResource(R.string.privacy_lock_mode_off)
                                    AppLockMode.PIN -> stringResource(R.string.privacy_lock_mode_pin)
                                    AppLockMode.BIOMETRIC -> stringResource(R.string.privacy_lock_mode_biometric)
                                },
                            )
                        },
                        modifier = Modifier.padding(end = MahinSpacing.sm),
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.pinDraft,
                    onValueChange = viewModel::onPinDraftChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = MahinSpacing.sm),
                    label = { Text(stringResource(R.string.privacy_set_pin)) },
                    placeholder = { Text(stringResource(R.string.privacy_pin_hint)) },
                )
                TextButton(onClick = viewModel::savePin) {
                    Text(stringResource(R.string.privacy_set_pin))
                }
            }
            item {
                SettingSwitchRow(
                    label = stringResource(R.string.privacy_hide_recents),
                    checked = state.hideRecents,
                    onCheckedChange = viewModel::setHideRecents,
                )
                SettingSwitchRow(
                    label = stringResource(R.string.privacy_block_screenshots),
                    checked = state.blockScreenshots,
                    onCheckedChange = viewModel::setBlockScreenshots,
                )
            }
            item {
                TextButton(onClick = viewModel::showEraseConfirm) {
                    Text(stringResource(R.string.privacy_erase_local))
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.sm),
        ) {
            Text(text = label, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
