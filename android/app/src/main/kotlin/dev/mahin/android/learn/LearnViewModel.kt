package dev.mahin.android.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.content.PersianSearchNormalizer
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LearnViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val cachePreferences: ContentCachePreferencesRepository,
        private val cycleRepository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LearnUiState())
        val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val bookmarks = cachePreferences.localBookmarkIds()
                _uiState.update { it.copy(bookmarkedIds = bookmarks) }
            }
            viewModelScope.launch {
                combine(
                    cycleRepository.observeDashboard(localDateClock.today()),
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { dashboard, profile, pregnancy ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    val pregnancyWeek =
                        if (mode == ReproductiveMode.PREGNANT && pregnancy != null) {
                            PregnancyDatingEngineV1
                                .status(
                                    lmpDate = pregnancy.lmpDate,
                                    clinicalEddDate = pregnancy.clinicalEddDate,
                                    asOfDate = localDateClock.today(),
                                ).displayWeekNumber
                        } else {
                            null
                        }
                    mode to pregnancyWeek
                }.distinctUntilChanged()
                    .collect { (mode, pregnancyWeek) ->
                        refreshMagazine(reproductiveMode = mode, pregnancyWeek = pregnancyWeek)
                    }
            }
        }

        fun onQueryChange(value: String) {
            _uiState.update { it.copy(query = value) }
        }

        fun search() {
            val query = _uiState.value.query
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, errorMessage = null) }
                val normalized = PersianSearchNormalizer.normalize(query)
                runCatching { contentRepository.search(normalized) }
                    .onSuccess { items ->
                        _uiState.update { it.copy(loading = false, searchResults = items, offline = false) }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                errorMessage = "learn_offline",
                                searchResults = emptyList(),
                                offline = true,
                            )
                        }
                    }
            }
        }

        fun selectCategory(category: LearnCategory?) {
            _uiState.update { it.copy(selectedCategory = category) }
            viewModelScope.launch { loadCategoryCollections(category) }
        }

        fun refreshMagazine(
            reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
            pregnancyWeek: Int? = null,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, errorMessage = null) }
                val featured =
                    runCatching { contentRepository.collections() }
                        .getOrElse { emptyList() }
                        .filter { it.featured }
                val programs = runCatching { contentRepository.programs() }.getOrElse { emptyList() }
                val contextualLifeStage =
                    when (reproductiveMode) {
                        ReproductiveMode.PREGNANT -> "pregnancy"
                        ReproductiveMode.TRYING_TO_CONCEIVE -> "ttc"
                        else -> "cycle"
                    }
                val contextual =
                    runCatching {
                        contentRepository.search(
                            query = "",
                            lifeStage = contextualLifeStage,
                        )
                    }.getOrElse { emptyList() }
                        .let { items ->
                            if (pregnancyWeek != null) {
                                items.filter { it.gestationalWeek == pregnancyWeek }.ifEmpty { items }
                            } else {
                                items
                            }
                        }
                val recentIds = contentRepository.recentArticleIds()
                val recentArticles = recentIds.mapNotNull { id -> resolveSummary(id, contextual) }
                _uiState.update {
                    it.copy(
                        loading = false,
                        featuredCollections = featured,
                        programs = programs,
                        contextualArticles = contextual.take(6),
                        recentArticles = recentArticles,
                        offline = featured.isEmpty() && programs.isEmpty() && contextual.isEmpty(),
                    )
                }
                loadCategoryCollections(_uiState.value.selectedCategory)
            }
        }

        private suspend fun loadCategoryCollections(category: LearnCategory?) {
            val items =
                runCatching {
                    contentRepository.collections(category?.apiValue)
                }.getOrElse { emptyList() }
            _uiState.update { it.copy(categoryCollections = items) }
        }

        private suspend fun resolveSummary(
            id: String,
            contextual: List<ContentArticleSummaryDto>,
        ): ContentArticleSummaryDto? {
            contextual.firstOrNull { it.id == id }?.let { return it }
            val article = contentRepository.article(id)
            return article?.let {
                ContentArticleSummaryDto(
                    id = it.id,
                    slug = it.slug,
                    locale = it.locale,
                    title = it.title,
                    summary = it.summary,
                    contentType = it.contentType,
                )
            }
        }

        fun toggleBookmark(documentId: String) {
            viewModelScope.launch {
                cachePreferences.toggleLocalBookmark(documentId)
                val bookmarks = cachePreferences.localBookmarkIds()
                _uiState.update { it.copy(bookmarkedIds = bookmarks) }
            }
        }
    }
