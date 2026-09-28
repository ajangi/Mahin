package dev.mahin.android.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.designsystem.MahinSpacing

@Composable
fun AppLockGate(
    modifier: Modifier = Modifier,
    viewModel: AppLockViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity
    if (!state.visible) {
        content()
        return
    }
    SensitiveScreenProtection(enabled = true)
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(MahinSpacing.lg)) {
            Text(
                text = stringResource(R.string.app_lock_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            if (state.mode == AppLockMode.PIN || state.mode == AppLockMode.BIOMETRIC) {
                OutlinedTextField(
                    value = state.pinEntry,
                    onValueChange = viewModel::onPinChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = MahinSpacing.md),
                    label = { Text(stringResource(R.string.app_lock_pin_prompt)) },
                )
                if (state.error) {
                    Text(
                        text = stringResource(R.string.app_lock_invalid_pin),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(onClick = viewModel::submitPin, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.app_lock_unlock))
                }
            }
            if (state.mode == AppLockMode.BIOMETRIC) {
                TextButton(
                    onClick = { viewModel.launchBiometric(activity) { } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.app_lock_use_biometric))
                }
            }
        }
    }
}
