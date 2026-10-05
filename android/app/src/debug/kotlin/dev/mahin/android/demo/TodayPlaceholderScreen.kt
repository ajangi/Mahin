package dev.mahin.android.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.mahinTextStyle

@Composable
fun TodayPlaceholderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(MahinSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.today_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.today_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.today_numeric_sample, PersianDigits.format(28)),
            style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
            color = MaterialTheme.colorScheme.primary,
        )
        MahinEmptyState(
            title = stringResource(R.string.today_empty_title),
            body = stringResource(R.string.today_empty_body),
        )
    }
}
