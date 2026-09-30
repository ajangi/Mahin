package dev.mahin.backend.content.persistence

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ContentSourceRepository : JpaRepository<ContentSourceEntity, UUID> {
    fun findByCitationKey(citationKey: String): Optional<ContentSourceEntity>
}

interface ContentDocumentRepository : JpaRepository<ContentDocumentEntity, UUID> {
    fun findBySlugAndLocale(
        slug: String,
        locale: String,
    ): Optional<ContentDocumentEntity>
}

interface ContentVersionRepository : JpaRepository<ContentVersionEntity, UUID> {
    fun findByDocumentIdOrderByVersionNumberDesc(documentId: UUID): List<ContentVersionEntity>

    fun findTopByDocumentIdOrderByVersionNumberDesc(documentId: UUID): Optional<ContentVersionEntity>

    @Query(
        """
        SELECT v FROM ContentVersionEntity v
        JOIN ContentDocumentEntity d ON d.id = v.documentId
        WHERE d.publishedVersionId = v.id
          AND d.withdrawnAt IS NULL
          AND v.status = 'published'
          AND (:locale IS NULL OR d.locale = :locale)
          AND (:lifeStage IS NULL OR v.lifeStage = :lifeStage)
          AND (
            :query IS NULL OR
            LOWER(v.searchIndexText) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY v.title ASC
        """,
    )
    fun searchPublished(
        @Param("query") query: String?,
        @Param("locale") locale: String?,
        @Param("lifeStage") lifeStage: String?,
    ): List<ContentVersionEntity>

    @Query(
        """
        SELECT v FROM ContentVersionEntity v
        JOIN ContentDocumentEntity d ON d.id = v.documentId
        WHERE d.publishedVersionId = v.id
          AND d.withdrawnAt IS NULL
          AND v.status = 'published'
          AND v.contentType = 'pregnancy_week'
          AND v.gestationalWeek = :week
          AND d.locale = :locale
        """,
    )
    fun findPublishedPregnancyWeek(
        @Param("week") week: Int,
        @Param("locale") locale: String,
    ): Optional<ContentVersionEntity>
}

interface ContentCatalogStateRepository : JpaRepository<ContentCatalogStateEntity, Int>

interface ContentAuditEventRepository : JpaRepository<ContentAuditEventEntity, UUID> {
    fun findByDocumentIdOrderByCreatedAtDesc(documentId: UUID): List<ContentAuditEventEntity>
}

interface UserContentBookmarkRepository : JpaRepository<UserContentBookmarkEntity, UserContentBookmarkEntity.Pk> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<UserContentBookmarkEntity>

    fun existsByUserIdAndDocumentId(
        userId: UUID,
        documentId: UUID,
    ): Boolean

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserContentBookmarkEntity b where b.userId = :userId")
    fun deleteAllByUserId(userId: UUID)
}
