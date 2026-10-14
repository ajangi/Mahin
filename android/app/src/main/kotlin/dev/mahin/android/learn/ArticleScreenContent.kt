package dev.mahin.android.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinSurfaceCard

@Composable
fun ArticleScreenContent(
    state: ArticleUiState,
    modifier: Modifier = Modifier,
) {
    val article = state.article
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md)
                .testTag("learn_article_screen"),
    ) {
        if (article == null && state.loading) {
            Text(text = stringResource(R.string.learn_loading))
            return
        }
        if (article == null) {
            Text(text = stringResource(R.string.learn_offline_title))
            return
        }
        Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
        article.summary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = MahinSpacing.sm),
            )
        }
        article.body?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        MahinSurfaceCard(
            modifier =
                Modifier
                    .padding(top = MahinSpacing.lg)
                    .testTag("learn_article_review_footer"),
        ) {
            Column(modifier = Modifier.padding(MahinSpacing.md)) {
                Text(
                    text = stringResource(R.string.learn_review_footer_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (article.sourceReferences.isNotEmpty()) {
                    Text(
                        text =
                            stringResource(
                                R.string.learn_review_sources,
                                article.sourceReferences.joinToString(separator = " · "),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                article.clinicalReviewer?.let { reviewer ->
                    Text(
                        text = stringResource(R.string.learn_review_reviewer, reviewer),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
