package dev.mahin.android.learn

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.content.ContentCollectionSummaryDto
import dev.mahin.core.content.ContentProgramSummaryDto
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.media.illustration.MahinIllustration
import dev.mahin.core.media.illustration.MahinIllustrationSource

@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun LearnScreenContent(
    state: LearnUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onSelectCategory: (LearnCategory?) -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenProgram: (String) -> Unit,
    onOpenCollection: (String) -> Unit,
    illustrationSource: MahinIllustrationSource,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(MahinSpacing.md).testTag("learn_magazine_root"),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        Text(text = stringResource(R.string.learn_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.learn_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.learn_search_label)) },
            singleLine = true,
        )
        TextButton(onClick = onSearch) {
            Text(stringResource(R.string.learn_search_action))
        }
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.testTag("learn_loading"))
        }
        if (state.offline && state.featuredCollections.isEmpty()) {
            MahinEmptyState(
                title = stringResource(R.string.learn_offline_title),
                body = stringResource(R.string.learn_offline_body),
            )
        }
        LearnCategoryChips(selected = state.selectedCategory, onSelect = onSelectCategory)
        if (state.featuredCollections.isNotEmpty()) {
            Text(
                text = stringResource(R.string.learn_featured_collections),
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = MahinSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
                modifier = Modifier.testTag("learn_featured_carousel"),
            ) {
                items(state.featuredCollections, key = { it.id }) { collection ->
                    CollectionHeroCard(
                        collection = collection,
                        onClick = { onOpenCollection(collection.id) },
                    )
                }
            }
        }
        if (state.contextualArticles.isNotEmpty()) {
            Text(text = stringResource(R.string.learn_contextual_rail), style = MaterialTheme.typography.titleMedium)
            ArticleRail(
                articles = state.contextualArticles,
                bookmarkedIds = state.bookmarkedIds,
                onOpenArticle = onOpenArticle,
                onToggleBookmark = onToggleBookmark,
            )
        }
        if (state.programs.isNotEmpty()) {
            Text(text = stringResource(R.string.learn_programs_rail), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                items(state.programs, key = { it.id }) { program ->
                    ProgramCard(program = program, onClick = { onOpenProgram(program.id) })
                }
            }
        }
        if (state.recentArticles.isNotEmpty()) {
            Text(text = stringResource(R.string.learn_recent_rail), style = MaterialTheme.typography.titleMedium)
            ArticleRail(
                articles = state.recentArticles,
                bookmarkedIds = state.bookmarkedIds,
                onOpenArticle = onOpenArticle,
                onToggleBookmark = onToggleBookmark,
            )
        }
        if (state.searchResults.isNotEmpty()) {
            Text(text = stringResource(R.string.learn_search_results), style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                items(state.searchResults, key = { it.id }) { item ->
                    ArticleRow(
                        item = item,
                        bookmarked = item.id in state.bookmarkedIds,
                        onOpen = { onOpenArticle(item.id) },
                        onToggleBookmark = onToggleBookmark,
                    )
                }
            }
        }
        if (state.categoryCollections.isNotEmpty()) {
            Text(
                text = stringResource(R.string.learn_category_collections),
                style = MaterialTheme.typography.titleMedium,
            )
            state.categoryCollections.forEach { collection ->
                CollectionHeroCard(collection = collection, onClick = { onOpenCollection(collection.id) })
            }
        }
        MahinIllustration(
            assetId = "editorial/empty-state/v1",
            modifier = Modifier.padding(top = MahinSpacing.md),
            decorative = true,
            source = illustrationSource,
        )
    }
}

@Composable
private fun LearnCategoryChips(
    selected: LearnCategory?,
    onSelect: (LearnCategory?) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("learn_category_chips"),
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.learn_category_all)) },
        )
        LearnCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(stringResource(category.labelRes)) },
            )
        }
    }
}

private val LearnCategory.labelRes: Int
    get() =
        when (this) {
            LearnCategory.CYCLE -> R.string.learn_category_cycle
            LearnCategory.FERTILITY -> R.string.learn_category_fertility
            LearnCategory.PREGNANCY -> R.string.learn_category_pregnancy
            LearnCategory.SYMPTOMS -> R.string.learn_category_symptoms
            LearnCategory.NUTRITION -> R.string.learn_category_nutrition
            LearnCategory.TESTS -> R.string.learn_category_tests
            LearnCategory.LIFESTYLE -> R.string.learn_category_lifestyle
        }

@Composable
private fun CollectionHeroCard(
    collection: ContentCollectionSummaryDto,
    onClick: () -> Unit,
) {
    MahinSurfaceCard(modifier = Modifier.fillMaxWidth().testTag("learn_collection_card")) {
        TextButton(onClick = onClick) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = collection.title, style = MaterialTheme.typography.titleMedium)
                collection.summary?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: ContentProgramSummaryDto,
    onClick: () -> Unit,
) {
    MahinSurfaceCard(modifier = Modifier.testTag("learn_program_card")) {
        TextButton(onClick = onClick) {
            Text(text = program.title, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun ArticleRail(
    articles: List<ContentArticleSummaryDto>,
    bookmarkedIds: Set<String>,
    onOpenArticle: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        items(articles, key = { it.id }) { item ->
            ArticleRow(
                item = item,
                bookmarked = item.id in bookmarkedIds,
                onOpen = { onOpenArticle(item.id) },
                onToggleBookmark = onToggleBookmark,
            )
        }
    }
}

@Composable
private fun ArticleRow(
    item: ContentArticleSummaryDto,
    bookmarked: Boolean,
    onOpen: () -> Unit,
    onToggleBookmark: (String) -> Unit,
) {
    MahinSurfaceCard(modifier = Modifier.fillMaxWidth().testTag("learn_article_row")) {
        Column(modifier = Modifier.fillMaxWidth().padding(MahinSpacing.sm)) {
            TextButton(onClick = onOpen) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            }
            item.summary?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            IconButton(onClick = { onToggleBookmark(item.id) }) {
                Icon(
                    imageVector = if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.learn_bookmark_toggle),
                )
            }
        }
    }
}
