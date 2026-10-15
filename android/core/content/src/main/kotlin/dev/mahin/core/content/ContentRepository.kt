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

        override suspend fun fetchDailyTips(key: DailyTipTargetingKey): DailyTipFetchResult =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                runCatching {
                    api
                        .dailyTips(
                            locale = "fa-IR",
                            mode = key.mode,
                            cyclePhase = key.cyclePhase,
                            pregnancyWeek = key.pregnancyWeek,
                        ).items
                }.fold(
                    onSuccess = { DailyTipFetchResult.Success(it) },
                    onFailure = { DailyTipFetchResult.NetworkFailure(it) },
                )
            }

        override suspend fun cachedDailyTipFor(key: DailyTipTargetingKey): ContentDailyTipDto? =
            withContext(Dispatchers.IO) {
                cachePreferences
                    .cachedDailyTipEnvelopeJson()
                    ?.let { payload ->
                        runCatching { json.decodeFromString<CachedDailyTipEnvelope>(payload) }.getOrNull()
                    }?.takeIf { envelope -> envelope.key == key }
                    ?.tip
            }

        override suspend fun cacheDailyTip(
            tip: ContentDailyTipDto,
            key: DailyTipTargetingKey,
        ) {
            withContext(Dispatchers.IO) {
                val envelope = CachedDailyTipEnvelope(tip = tip, key = key)
                cachePreferences.setCachedDailyTipEnvelopeJson(json.encodeToString(envelope))
            }
        }

        override suspend fun clearCachedDailyTip() {
            withContext(Dispatchers.IO) {
                cachePreferences.clearCachedDailyTip()
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
                runCatching { api.program(documentId) }
                    .onSuccess { localStore.cacheProgram(it) }
                    .getOrElse { localStore.cachedProgram(documentId) }
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

        suspend fun careSchedules(gestationalWeek: Int? = null): List<ContentCareScheduleDto> =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                val summaries =
                    runCatching {
                        api.careSchedules(locale = "fa-IR", gestationalWeek = gestationalWeek).items
                    }.getOrElse { emptyList() }
                summaries.mapNotNull { summary ->
                    runCatching { api.careSchedule(summary.id) }.getOrNull()
                }
            }

        suspend fun checklistTemplate(kind: String = "hospital_bag"): ContentChecklistTemplateDto? =
            withContext(Dispatchers.IO) {
                awaitCatalogSync()
                val summary =
                    runCatching {
                        api.checklistTemplates(locale = "fa-IR", checklistKind = kind).items.firstOrNull()
                    }.getOrNull() ?: return@withContext null
                runCatching { api.checklistTemplate(summary.id) }.getOrNull()
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
