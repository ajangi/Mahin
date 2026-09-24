package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinThemeTokens
import dev.mahin.core.designsystem.R

@Composable
fun MahinErrorState(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.ds_error_title),
    body: String = stringResource(R.string.ds_error_body),
    onRetry: (() -> Unit)? = null,
    retryLabel: String = stringResource(R.string.ds_retry),
) {
    val extended = MahinThemeTokens.extendedColors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(MahinSpacing.lg)
                .semantics { contentDescription = title },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = extended.statusCritical,
        )
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onRetry != null) {
            MahinPrimaryButton(text = retryLabel, onClick = onRetry)
        }
    }
}
