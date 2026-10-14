package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.content.ContentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ArticleViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val contentRepository: ContentRepository,
        private val analytics: AnalyticsGateway,
    ) : ViewModel() {
        private val articleId: String = savedStateHandle.get<String>("articleId") ?: ""
        private val _uiState = MutableStateFlow(ArticleUiState())
        val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val article = contentRepository.article(articleId)
                if (article != null) {
                    contentRepository.recordArticleView(articleId)
                    analytics.track(AnalyticsEvent("article_opened", mapOf("surface" to "learn")))
                }
                _uiState.update {
                    it.copy(article = article, loading = false, offline = article == null)
                }
            }
        }
    }
