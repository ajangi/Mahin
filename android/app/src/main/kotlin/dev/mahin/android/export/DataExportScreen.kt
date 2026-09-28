package dev.mahin.android.export

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing

@Composable
fun DataExportScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataExportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md)
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.export_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.export_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = MahinSpacing.sm),
        )
        if (state.locked) {
            Text(
                text = stringResource(R.string.export_locked),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = viewModel::generateExport,
            enabled = !state.loading,
            modifier = Modifier.padding(vertical = MahinSpacing.sm),
        ) {
            Text(stringResource(R.string.export_generate))
        }
        state.exportJson?.let { json ->
            Button(
                onClick = {
                    val share =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                    context.startActivity(Intent.createChooser(share, context.getString(R.string.export_share_chooser)))
                },
            ) {
                Text(stringResource(R.string.export_share))
            }
            Text(
                text = json,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = MahinSpacing.md)) {
            Text(stringResource(R.string.export_close))
        }
    }
}
