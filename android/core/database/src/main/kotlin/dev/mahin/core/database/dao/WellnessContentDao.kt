package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.ContentArticleCacheEntity
import dev.mahin.core.database.entity.ContentRecentViewEntity
import dev.mahin.core.database.entity.GuidedPlayerStateEntity
import dev.mahin.core.database.entity.MediaAssetCacheEntity
import dev.mahin.core.database.entity.ProgramSessionCompletionEntity

@Dao
interface WellnessContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticleCache(entity: ContentArticleCacheEntity)

    @Query("SELECT * FROM content_article_cache WHERE documentId = :documentId LIMIT 1")
    suspend fun articleCache(documentId: String): ContentArticleCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordRecentView(entity: ContentRecentViewEntity)

    @Query("SELECT * FROM content_recent_view ORDER BY viewedAtEpochMs DESC LIMIT :limit")
    suspend fun recentViews(limit: Int): List<ContentRecentViewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaCache(entity: MediaAssetCacheEntity)

    @Query("SELECT * FROM media_asset_cache WHERE assetId = :assetId LIMIT 1")
    suspend fun mediaCache(assetId: String): MediaAssetCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerState(entity: GuidedPlayerStateEntity)

    @Query("SELECT * FROM guided_player_state WHERE id = :id LIMIT 1")
    suspend fun playerState(id: Int = GuidedPlayerStateEntity.SINGLE_ROW_ID): GuidedPlayerStateEntity?

    @Query("DELETE FROM guided_player_state WHERE id = :id")
    suspend fun clearPlayerState(id: Int = GuidedPlayerStateEntity.SINGLE_ROW_ID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(entity: ProgramSessionCompletionEntity)

    @Query("SELECT COUNT(*) FROM program_session_completion WHERE programId = :programId")
    suspend fun completionCountForProgram(programId: String): Int
}
