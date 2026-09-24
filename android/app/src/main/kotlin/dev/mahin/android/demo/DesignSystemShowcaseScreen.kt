package dev.mahin.android.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinErrorState
import dev.mahin.core.designsystem.component.MahinLoadingState
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle

@Composable
fun DesignSystemShowcaseScreen(modifier: Modifier = Modifier) {
    var errorRetries by remember { mutableIntStateOf(0) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.showcase_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.showcase_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MahinPrimaryButton(
            text = stringResource(R.string.showcase_primary_action),
            onClick = { },
        )
        HorizontalDivider()
        MahinLoadingState()
        HorizontalDivider()
        MahinErrorState(onRetry = { errorRetries += 1 })
        if (errorRetries > 0) {
            Text(
                text = stringResource(R.string.showcase_retry_count, errorRetries),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
