package dev.mahin.core.content

import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.domain.content.PersianSearchNormalizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ContentRepository
    @Inject
    constructor(
        private val api: ContentApi,
        private val cachePreferences: ContentCachePreferencesRepository,
    ) {
        suspend fun search(
            query: String,
            lifeStage: String? = null,
        ): List<ContentArticleSummaryDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                val normalized = PersianSearchNormalizer.normalize(query)
                api.search(normalized.ifBlank { null }, locale = "fa-IR", lifeStage = lifeStage).items
            }

        suspend fun pregnancyWeek(week: Int): ContentArticleDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.pregnancyWeek(week) }.getOrNull()
            }

        private suspend fun awaitCatalogSync() {
            val remote = api.catalogStatus()
            val localRevision = cachePreferences.getPublicationRevision()
            if (remote.publicationRevision > localRevision) {
                cachePreferences.clearCachedArticles()
                cachePreferences.setPublicationRevision(remote.publicationRevision)
            }
        }
    }
