package dev.mahin.android.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearnUiState(
    val query: String = "",
    val results: List<ContentArticleSummaryDto> = emptyList(),
    val localBookmarks: Set<String> = emptySet(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LearnViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val cachePreferences: ContentCachePreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LearnUiState())
        val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val bookmarks = cachePreferences.localBookmarkIds()
                _uiState.update { it.copy(localBookmarks = bookmarks) }
            }
        }

        fun onQueryChange(value: String) {
            _uiState.update { it.copy(query = value) }
        }

        fun search() {
            val query = _uiState.value.query
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, errorMessage = null) }
                runCatching { contentRepository.search(query) }
                    .onSuccess { items ->
                        _uiState.update { it.copy(loading = false, results = items) }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                errorMessage = "learn_offline",
                                results = emptyList(),
                            )
                        }
                    }
            }
        }

        fun toggleBookmark(documentId: String) {
            viewModelScope.launch {
                cachePreferences.toggleLocalBookmark(documentId)
                val bookmarks = cachePreferences.localBookmarkIds()
                _uiState.update { it.copy(localBookmarks = bookmarks) }
            }
        }
    }
