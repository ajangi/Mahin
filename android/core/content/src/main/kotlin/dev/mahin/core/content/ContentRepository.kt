package dev.mahin.core.content

import dev.mahin.core.content.di.IoDispatcher
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.domain.content.PersianSearchNormalizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class ContentRepository
    @Inject
    constructor(
        private val api: ContentApi,
        private val cachePreferences: ContentCachePreferencesRepository,
        private val localStore: WellnessContentLocalStore,
        private val json: Json,
        private val mediaMemoryCache: MediaAssetMemoryCache,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : DailyTipDataSource {
        suspend fun search(
            query: String,
            lifeStage: String? = null,
        ): List<ContentArticleSummaryDto> =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                val normalized = PersianSearchNormalizer.normalize(query)
                api.search(normalized.ifBlank { null }, locale = "fa-IR", lifeStage = lifeStage).items
            }

        suspend fun article(documentId: String): ContentArticleDto? =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.article(documentId) }
                    .onSuccess { localStore.cacheArticle(it) }
                    .getOrElse { localStore.cachedArticle(documentId) }
            }

        sealed interface PregnancyWeekFetch {
            data class Loaded(
                val article: ContentArticleDto?,
            ) : PregnancyWeekFetch

            data object NetworkError : PregnancyWeekFetch
        }

        suspend fun pregnancyWeek(week: Int): ContentArticleDto? =
            when (val result = pregnancyWeekFetch(week)) {
                is PregnancyWeekFetch.Loaded -> result.article
                is PregnancyWeekFetch.NetworkError -> null
            }

        suspend fun pregnancyWeekFetch(week: Int): PregnancyWeekFetch =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.pregnancyWeek(week) }.fold(
                    onSuccess = { PregnancyWeekFetch.Loaded(it) },
                    onFailure = { error ->
                        if (error is HttpException && error.code() == 404) {
                            PregnancyWeekFetch.Loaded(null)
                        } else {
                            PregnancyWeekFetch.NetworkError
                        }
                    },
                )
            }

        override suspend fun fetchDailyTips(key: DailyTipTargetingKey): DailyTipFetchResult =
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
                val envelope = CachedDailyTipEnvelope(tip = tip, key = key)
                cachePreferences.setCachedDailyTipEnvelopeJson(json.encodeToString(envelope))
            }
        }

        override suspend fun clearCachedDailyTip() {
            withContext(ioDispatcher) {
                cachePreferences.clearCachedDailyTip()
            }
        }

        suspend fun programs(programType: String? = null): List<ContentProgramSummaryDto> =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.programs(locale = "fa-IR", programType = programType).items }
                    .getOrElse { emptyList() }
            }

        suspend fun program(documentId: String): ContentProgramDto? =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.program(documentId) }
                    .onSuccess { localStore.cacheProgram(it) }
                    .getOrElse { localStore.cachedProgram(documentId) }
            }

        suspend fun collections(category: String? = null): List<ContentCollectionSummaryDto> =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.collections(locale = "fa-IR", category = category).items }
                    .getOrElse { emptyList() }
            }

        suspend fun collection(documentId: String): ContentCollectionDto? =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                runCatching { api.collection(documentId) }.getOrNull()
            }

        suspend fun careSchedules(gestationalWeek: Int? = null): List<ContentCareScheduleDto> =
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
                awaitCatalogSync()
                val summary =
                    runCatching {
                        api.checklistTemplates(locale = "fa-IR", checklistKind = kind).items.firstOrNull()
                    }.getOrNull() ?: return@withContext null
                runCatching { api.checklistTemplate(summary.id) }.getOrNull()
            }

        sealed interface WarningSignsCardFetch {
            data class Network(
                val card: ContentWarningSignsDto,
            ) : WarningSignsCardFetch

            data class Cache(
                val card: ContentWarningSignsDto,
            ) : WarningSignsCardFetch
        }

        suspend fun warningSignsCard(): ContentWarningSignsDto? =
            warningSignsCardFetch()?.let { fetch ->
                when (fetch) {
                    is WarningSignsCardFetch.Network -> fetch.card
                    is WarningSignsCardFetch.Cache -> fetch.card
                }
            }

        suspend fun warningSignsCardFetch(): WarningSignsCardFetch? =
            withContext(ioDispatcher) {
                awaitCatalogSync()
                val listResponse =
                    runCatching { api.warningSigns(locale = "fa-IR") }.getOrElse {
                        return@withContext cachedWarningSignsCard()?.let(WarningSignsCardFetch::Cache)
                    }
                val summary = listResponse.items.firstOrNull()
                if (summary == null) {
                    cachePreferences.clearCachedWarningSignsCard()
                    return@withContext null
                }
                val fetched =
                    runCatching { api.warningSignsDetail(summary.id) }
                        .onSuccess { card ->
                            cachePreferences.setCachedWarningSignsCardJson(json.encodeToString(card))
                        }.getOrNull()
                when {
                    fetched != null -> WarningSignsCardFetch.Network(fetched)
                    else -> cachedWarningSignsCard()?.let(WarningSignsCardFetch::Cache)
                }
            }

        suspend fun cachedWarningSignsCard(): ContentWarningSignsDto? =
            withContext(ioDispatcher) {
                cachePreferences.cachedWarningSignsCardJson()?.let { payload ->
                    runCatching { json.decodeFromString<ContentWarningSignsDto>(payload) }.getOrNull()
                }
            }

        suspend fun mediaAsset(assetId: String): MediaAssetDto? =
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
                localStore.recordRecentView(documentId)
            }
        }

        suspend fun recentArticleIds(): List<String> =
            withContext(ioDispatcher) {
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
