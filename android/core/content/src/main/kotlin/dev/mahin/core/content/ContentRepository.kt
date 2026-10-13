package dev.mahin.core.content

import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.domain.content.PersianSearchNormalizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class ContentRepository
    @Inject
    constructor(
        private val api: ContentApi,
        private val cachePreferences: ContentCachePreferencesRepository,
        private val localStore: WellnessContentLocalStore,
        private val json: Json,
        private val mediaMemoryCache: MediaAssetMemoryCache,
    ) : DailyTipDataSource {
        suspend fun search(
            query: String,
            lifeStage: String? = null,
        ): List<ContentArticleSummaryDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                val normalized = PersianSearchNormalizer.normalize(query)
                api.search(normalized.ifBlank { null }, locale = "fa-IR", lifeStage = lifeStage).items
            }

        suspend fun article(documentId: String): ContentArticleDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.article(documentId) }
                    .onSuccess { localStore.cacheArticle(it) }
                    .getOrElse { localStore.cachedArticle(documentId) }
            }

        suspend fun pregnancyWeek(week: Int): ContentArticleDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.pregnancyWeek(week) }.getOrNull()
            }

        override suspend fun dailyTips(
            mode: String?,
            cyclePhase: String?,
            pregnancyWeek: Int?,
        ): List<ContentDailyTipDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching {
                    api
                        .dailyTips(
                            locale = "fa-IR",
                            mode = mode,
                            cyclePhase = cyclePhase,
                            pregnancyWeek = pregnancyWeek,
                        ).items
                }.getOrElse { emptyList() }
            }

        override suspend fun cachedDailyTip(): ContentDailyTipDto? =
            withContext(Dispatchers.IO) {
                cachePreferences.cachedDailyTipJson()?.let { payload ->
                    runCatching { json.decodeFromString<ContentDailyTipDto>(payload) }.getOrNull()
                }
            }

        override suspend fun cacheDailyTip(tip: ContentDailyTipDto) {
            withContext(Dispatchers.IO) {
                cachePreferences.setCachedDailyTipJson(json.encodeToString(tip))
            }
        }

        suspend fun programs(programType: String? = null): List<ContentProgramSummaryDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.programs(locale = "fa-IR", programType = programType).items }
                    .getOrElse { emptyList() }
            }

        suspend fun program(documentId: String): ContentProgramDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.program(documentId) }.getOrNull()
            }

        suspend fun collections(category: String? = null): List<ContentCollectionSummaryDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.collections(locale = "fa-IR", category = category).items }
                    .getOrElse { emptyList() }
            }

        suspend fun collection(documentId: String): ContentCollectionDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching { api.collection(documentId) }.getOrNull()
            }

        suspend fun mediaAsset(assetId: String): MediaAssetDto? =
            withContext(Dispatchers.IO) {
                mediaMemoryCache.get(assetId)?.let { return@withContext it }
                val resolved =
                    runCatching { api.mediaAsset(assetId) }
                        .onSuccess {
                            localStore.cacheMediaAsset(it)
                            mediaMemoryCache.put(it)
                        }.getOrElse { localStore.cachedMediaAsset(assetId) }
                resolved?.let { mediaMemoryCache.put(it) }
                resolved
            }

        fun cachedMediaAssetInMemory(assetId: String): MediaAssetDto? = mediaMemoryCache.get(assetId)

        suspend fun recordArticleView(documentId: String) {
            withContext(Dispatchers.IO) {
                localStore.recordRecentView(documentId)
            }
        }

        suspend fun recentArticleIds(): List<String> =
            withContext(Dispatchers.IO) {
                localStore.recentArticleIds()
            }

        private suspend fun awaitCatalogSync() {
            val remote =
                runCatching { api.catalogStatus() }.getOrNull() ?: return
            val localRevision = cachePreferences.getPublicationRevision()
            if (remote.publicationRevision > localRevision) {
                cachePreferences.clearCachedArticles()
                cachePreferences.setPublicationRevision(remote.publicationRevision)
            }
        }
    }
