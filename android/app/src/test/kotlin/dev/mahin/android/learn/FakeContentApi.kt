package dev.mahin.android.learn

import dev.mahin.core.content.ContentApi
import dev.mahin.core.content.ContentArticleDto
import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.content.ContentCatalogStatusDto
import dev.mahin.core.content.ContentCollectionDto
import dev.mahin.core.content.ContentCollectionListResponseDto
import dev.mahin.core.content.ContentCollectionSummaryDto
import dev.mahin.core.content.ContentDailyTipListResponseDto
import dev.mahin.core.content.ContentProgramDto
import dev.mahin.core.content.ContentProgramListResponseDto
import dev.mahin.core.content.ContentSearchResponseDto
import dev.mahin.core.content.MediaAssetDto
import dev.mahin.core.content.ProgramSessionDto
import dev.mahin.core.content.ProgramStepDto

class FakeContentApi : ContentApi {
    var throwOnPrograms: Boolean = false
    var throwOnSearch: Boolean = false
    var throwOnArticle: Boolean = false
    var throwOnPregnancyWeek: Boolean = false
    var pregnancyWeekArticles: MutableMap<Int, ContentArticleDto> = mutableMapOf()
    var throwOnProgram: Boolean = false
    var articles: MutableMap<String, ContentArticleDto> = mutableMapOf()
    var programs: MutableMap<String, ContentProgramDto> = mutableMapOf()
    var collections: List<ContentCollectionSummaryDto> = emptyList()
    var searchResults: List<ContentArticleSummaryDto> = emptyList()
    var lastSearchQuery: String? = null
    var lastSearchLifeStage: String? = null
    var careScheduleSummaries: List<dev.mahin.core.content.ContentCareScheduleSummaryDto> = emptyList()
    var careSchedulesById: MutableMap<String, dev.mahin.core.content.ContentCareScheduleDto> = mutableMapOf()
    var throwOnWarningSigns: Boolean = false
    var warningSignSummaries: List<dev.mahin.core.content.ContentWarningSignsSummaryDto> = emptyList()
    var warningSignsById: MutableMap<String, dev.mahin.core.content.ContentWarningSignsDto> = mutableMapOf()

    override suspend fun catalogStatus(): ContentCatalogStatusDto =
        ContentCatalogStatusDto(publicationRevision = 0L, updatedAt = "2025-01-01T00:00:00Z")

    override suspend fun search(
        query: String?,
        locale: String?,
        lifeStage: String?,
    ): ContentSearchResponseDto {
        if (throwOnSearch) error("offline")
        lastSearchQuery = query
        lastSearchLifeStage = lifeStage
        return ContentSearchResponseDto(items = searchResults)
    }

    override suspend fun article(articleId: String): ContentArticleDto {
        if (throwOnArticle) error("offline")
        return articles[articleId] ?: error("missing")
    }

    override suspend fun pregnancyWeek(
        week: Int,
        locale: String,
    ): ContentArticleDto {
        if (throwOnPregnancyWeek) error("offline")
        return pregnancyWeekArticles[week] ?: error("missing week $week")
    }

    override suspend fun dailyTips(
        locale: String,
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ): ContentDailyTipListResponseDto = ContentDailyTipListResponseDto(emptyList())

    override suspend fun programs(
        locale: String,
        programType: String?,
    ): ContentProgramListResponseDto {
        if (throwOnPrograms) error("offline")
        return ContentProgramListResponseDto(programs.values.map { it.toSummary() })
    }

    override suspend fun program(documentId: String): ContentProgramDto {
        if (throwOnProgram) error("offline")
        return programs[documentId] ?: error("missing")
    }

    override suspend fun collections(
        locale: String,
        category: String?,
    ): ContentCollectionListResponseDto = ContentCollectionListResponseDto(collections)

    override suspend fun collection(documentId: String): ContentCollectionDto = error("unused")

    override suspend fun mediaAsset(assetId: String): MediaAssetDto = error("unused")

    override suspend fun careSchedules(
        locale: String,
        gestationalWeek: Int?,
    ): dev.mahin.core.content.ContentCareScheduleListResponseDto =
        dev.mahin.core.content
            .ContentCareScheduleListResponseDto(careScheduleSummaries)

    override suspend fun careSchedule(documentId: String): dev.mahin.core.content.ContentCareScheduleDto =
        careSchedulesById[documentId] ?: error("missing care schedule $documentId")

    override suspend fun checklistTemplates(
        locale: String,
        checklistKind: String?,
    ): dev.mahin.core.content.ContentChecklistTemplateListResponseDto =
        dev.mahin.core.content
            .ContentChecklistTemplateListResponseDto(emptyList())

    override suspend fun checklistTemplate(documentId: String): dev.mahin.core.content.ContentChecklistTemplateDto =
        error("unused")

    override suspend fun warningSigns(locale: String): dev.mahin.core.content.ContentWarningSignsListResponseDto {
        if (throwOnWarningSigns) error("offline")
        return dev.mahin.core.content
            .ContentWarningSignsListResponseDto(warningSignSummaries)
    }

    override suspend fun warningSignsDetail(documentId: String): dev.mahin.core.content.ContentWarningSignsDto {
        if (throwOnWarningSigns) error("offline")
        return warningSignsById[documentId] ?: error("missing warning signs $documentId")
    }

    private fun ContentProgramDto.toSummary() =
        dev.mahin.core.content.ContentProgramSummaryDto(
            id = id,
            slug = slug,
            locale = locale,
            title = title,
            summary = summary,
            programType = programType,
        )
}

fun sampleProgram(
    programId: String = "prog-1",
    sessionId: String = "sess-1",
): ContentProgramDto =
    ContentProgramDto(
        id = programId,
        slug = "prog",
        locale = "fa-IR",
        title = "برنامه",
        programType = "pelvic_floor",
        medicalRiskLevel = "none",
        contentVersion = 1,
        sessions =
            listOf(
                ProgramSessionDto(
                    id = sessionId,
                    title = "جلسه",
                    order = 1,
                    steps =
                        listOf(
                            ProgramStepDto(instruction = "قدم اول", durationSeconds = 2),
                            ProgramStepDto(instruction = "قدم دوم", durationSeconds = 2),
                        ),
                ),
            ),
    )
