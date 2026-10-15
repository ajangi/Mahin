package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.media.illustration.MahinIllustration
import dev.mahin.core.media.illustration.BundledMahinIllustrationCatalog
import dev.mahin.core.media.illustration.MahinIllustrationSource

data class PregnancyWeekPageState(
    val weekNumber: Int,
    val title: String?,
    val fetalSummary: String?,
    val maternalSummary: String?,
    val sizeComparisonLabel: String?,
    val illustrationAssetId: String?,
)

data class PregnancyWeekByWeekUiState(
    val currentWeek: Int = 1,
    val pages: List<PregnancyWeekPageState> = emptyList(),
    val isLoading: Boolean = true,
)

@Composable
fun PregnancyWeekByWeekScreen(
    modifier: Modifier = Modifier,
    illustrationSource: MahinIllustrationSource = BundledMahinIllustrationCatalog,
    viewModel: PregnancyWeekByWeekViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyWeekByWeekScreenContent(
        state = state,
        illustrationSource = illustrationSource,
        onWeekSelected = viewModel::onWeekSelected,
        modifier = modifier,
    )
}

@Composable
fun PregnancyWeekByWeekScreenContent(
    state: PregnancyWeekByWeekUiState,
    illustrationSource: MahinIllustrationSource,
    onWeekSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialPage = (state.currentWeek - 1).coerceIn(0, 41)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 42 })
    LaunchedEffect(pagerState.currentPage) {
        onWeekSelected(pagerState.currentPage + 1)
    }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_week_by_week")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_week_by_week_title),
            subtitle = stringResource(R.string.pregnancy_week_by_week_subtitle),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val week = pageIndex + 1
            val page = state.pages.find { it.weekNumber == week }
            WeekPageCard(
                weekNumber = week,
                page = page,
                illustrationSource = illustrationSource,
            )
        }
    }
}

@Composable
private fun WeekPageCard(
    weekNumber: Int,
    page: PregnancyWeekPageState?,
    illustrationSource: MahinIllustrationSource,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.sm)
                .testTag("pregnancy_week_page_$weekNumber"),
    ) {
        Column(
            modifier = Modifier.padding(MahinSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_week_card_title,
                        PregnancyFormatters.formatInteger(weekNumber),
                    ),
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            )
            val assetId =
                page?.illustrationAssetId
                    ?: "pregnancy/fetal-development/week-$weekNumber/v1"
            MahinIllustration(
                assetId = assetId,
                source = illustrationSource,
                modifier = Modifier.fillMaxWidth(),
            )
            page?.sizeComparisonLabel?.let {
                Text(
                    text = stringResource(R.string.pregnancy_size_comparison, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = page?.fetalSummary ?: stringResource(R.string.pregnancy_week_placeholder_fetal),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = page?.maternalSummary ?: stringResource(R.string.pregnancy_week_placeholder_maternal),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
