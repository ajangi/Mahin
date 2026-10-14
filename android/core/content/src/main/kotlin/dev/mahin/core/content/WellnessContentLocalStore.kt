package dev.mahin.core.content

import dev.mahin.core.database.dao.WellnessContentDao
import dev.mahin.core.database.entity.ContentArticleCacheEntity
import dev.mahin.core.database.entity.ContentProgramCacheEntity
import dev.mahin.core.database.entity.ContentRecentViewEntity
import dev.mahin.core.database.entity.GuidedPlayerStateEntity
import dev.mahin.core.database.entity.MediaAssetCacheEntity
import dev.mahin.core.database.entity.ProgramSessionCompletionEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class WellnessContentLocalStore
    @Inject
    constructor(
        private val dao: WellnessContentDao,
        private val json: Json,
    ) {
        suspend fun cacheArticle(article: ContentArticleDto) {
            dao.upsertArticleCache(
                ContentArticleCacheEntity(
                    documentId = article.id,
                    payloadJson = json.encodeToString(article),
                    cachedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun cachedArticle(documentId: String): ContentArticleDto? =
            dao.articleCache(documentId)?.let { row ->
                runCatching { json.decodeFromString<ContentArticleDto>(row.payloadJson) }.getOrNull()
            }

        suspend fun recordRecentView(documentId: String) {
            dao.recordRecentView(
                ContentRecentViewEntity(
                    documentId = documentId,
                    viewedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun recentArticleIds(limit: Int = 12): List<String> = dao.recentViews(limit).map { it.documentId }

        suspend fun cacheMediaAsset(asset: MediaAssetDto) {
            dao.upsertMediaCache(
                MediaAssetCacheEntity(
                    assetId = asset.id,
                    payloadJson = json.encodeToString(asset),
                    cachedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun cachedMediaAsset(assetId: String): MediaAssetDto? =
            dao.mediaCache(assetId)?.let { row ->
                runCatching { json.decodeFromString<MediaAssetDto>(row.payloadJson) }.getOrNull()
            }

        suspend fun savePlayerState(
            programId: String,
            sessionId: String,
            stepIndex: Int,
            stepElapsedMs: Long,
            paused: Boolean,
        ) {
            dao.upsertPlayerState(
                GuidedPlayerStateEntity(
                    programId = programId,
                    sessionId = sessionId,
                    stepIndex = stepIndex,
                    stepElapsedMs = stepElapsedMs,
                    paused = paused,
                    updatedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun loadPlayerState(): GuidedPlayerStateEntity? = dao.playerState()

        suspend fun clearPlayerState() {
            dao.clearPlayerState()
        }

        suspend fun logSessionCompletion(
            id: String,
            programId: String,
            sessionId: String,
        ) {
            dao.insertCompletion(
                ProgramSessionCompletionEntity(
                    id = id,
                    programId = programId,
                    sessionId = sessionId,
                    completedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun programCompletionCount(programId: String): Int = dao.completionCountForProgram(programId)

        suspend fun cacheProgram(program: ContentProgramDto) {
            dao.upsertProgramCache(
                ContentProgramCacheEntity(
                    documentId = program.id,
                    payloadJson = json.encodeToString(program),
                    cachedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        suspend fun cachedProgram(documentId: String): ContentProgramDto? =
            dao.programCache(documentId)?.let { row ->
                runCatching { json.decodeFromString<ContentProgramDto>(row.payloadJson) }.getOrNull()
            }
    }
