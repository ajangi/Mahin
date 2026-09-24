package dev.mahin.android.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinThemeTokens

/**
 * Engineering-foundation surface only. Not a product feature screen; M1 replaces
 * this with the navigation shell.
 */
@Composable
fun FoundationScreen() {
    val colors = MahinThemeTokens.extendedColors
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(MahinSpacing.lg)
                .semantics { contentDescription = "mahin_foundation" },
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.foundation_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.foundation_privacy),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.foundation_tokens_note),
            style = MaterialTheme.typography.labelLarge,
            color = colors.healthFertility,
        )
    }
}
