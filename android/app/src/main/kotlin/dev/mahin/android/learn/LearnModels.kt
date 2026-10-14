package dev.mahin.android.learn

import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.content.ContentCollectionSummaryDto
import dev.mahin.core.content.ContentProgramSummaryDto

enum class LearnCategory(
    val apiValue: String,
) {
    CYCLE("cycle"),
    FERTILITY("fertility"),
    PREGNANCY("pregnancy"),
    SYMPTOMS("symptoms"),
    NUTRITION("nutrition"),
    TESTS("tests"),
    LIFESTYLE("lifestyle"),
}

data class ArticleUiState(
    val article: dev.mahin.core.content.ContentArticleDto? = null,
    val loading: Boolean = true,
    val offline: Boolean = false,
)

data class ProgramDetailUiState(
    val program: dev.mahin.core.content.ContentProgramDto? = null,
    val completionCount: Int = 0,
    val loading: Boolean = true,
)

data class GuidedPlayerUiState(
    val sessionTitle: String,
    val stepInstruction: String,
    val stepIndex: Int,
    val stepCount: Int,
    val progress: Float,
    val paused: Boolean,
    val completed: Boolean,
    val offline: Boolean = false,
    val loadError: Boolean = false,
    val snapshot: dev.mahin.domain.content.GuidedPlayerSnapshot,
)

data class LearnUiState(
    val query: String = "",
    val searchResults: List<ContentArticleSummaryDto> = emptyList(),
    val featuredCollections: List<ContentCollectionSummaryDto> = emptyList(),
    val categoryCollections: List<ContentCollectionSummaryDto> = emptyList(),
    val selectedCategory: LearnCategory? = null,
    val contextualArticles: List<ContentArticleSummaryDto> = emptyList(),
    val programs: List<ContentProgramSummaryDto> = emptyList(),
    val recentArticles: List<ContentArticleSummaryDto> = emptyList(),
    val bookmarkedIds: Set<String> = emptySet(),
    val loading: Boolean = false,
    val offline: Boolean = false,
    val errorMessage: String? = null,
)
