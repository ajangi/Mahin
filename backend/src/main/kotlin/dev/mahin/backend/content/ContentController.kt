package dev.mahin.backend.content

import java.util.UUID
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/content")
class ContentController(
    private val publicService: ContentPublicService,
    private val wellnessPublicService: ContentWellnessPublicService,
    private val pregnancyCompanionPublicService: ContentPregnancyCompanionPublicService,
) {
    @GetMapping("/catalog-status")
    fun catalogStatus(): ContentCatalogStatusResponse = publicService.catalogStatus()

    @GetMapping("/articles/{articleId}")
    fun getArticle(
        @PathVariable articleId: UUID,
    ): ContentArticleResponse = publicService.getArticle(articleId)

    @GetMapping("/articles/by-slug/{slug}")
    fun getBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentArticleResponse = publicService.getArticleBySlug(slug, locale)

    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) locale: String?,
        @RequestParam(required = false) lifeStage: String?,
    ): ContentSearchResponse = publicService.search(q, locale, lifeStage)

    @GetMapping("/pregnancy/weeks")
    fun listPregnancyWeeks(
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): PregnancyWeekListResponse = publicService.listPregnancyWeeks(locale)

    @GetMapping("/pregnancy/weeks/{week}")
    fun pregnancyWeek(
        @PathVariable week: Int,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentArticleResponse = publicService.pregnancyWeek(week, locale)

    @GetMapping("/bookmarks")
    fun bookmarks(): ContentBookmarkListResponse = publicService.listBookmarks()

    @PostMapping("/bookmarks/{documentId}")
    fun addBookmark(
        @PathVariable documentId: UUID,
    ) {
        publicService.addBookmark(documentId)
    }

    @DeleteMapping("/bookmarks/{documentId}")
    fun removeBookmark(
        @PathVariable documentId: UUID,
    ) {
        publicService.removeBookmark(documentId)
    }

    @GetMapping("/daily-tips")
    fun listDailyTips(
        @RequestParam(defaultValue = "fa-IR") locale: String,
        @RequestParam(required = false) mode: String?,
        @RequestParam(required = false) cyclePhase: String?,
        @RequestParam(required = false) pregnancyWeek: Int?,
    ): ContentDailyTipListResponse {
        ContentWellnessQueryValidator.validateDailyTipQuery(mode, cyclePhase, pregnancyWeek)
        return wellnessPublicService.listDailyTips(locale, mode, cyclePhase, pregnancyWeek)
    }

    @GetMapping("/daily-tips/{documentId}")
    fun getDailyTip(
        @PathVariable documentId: UUID,
    ): ContentDailyTipResponse = wellnessPublicService.getDailyTip(documentId)

    @GetMapping("/daily-tips/by-slug/{slug}")
    fun getDailyTipBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentDailyTipResponse = wellnessPublicService.getDailyTipBySlug(slug, locale)

    @GetMapping("/programs")
    fun listPrograms(
        @RequestParam(defaultValue = "fa-IR") locale: String,
        @RequestParam(required = false) programType: String?,
    ): ContentProgramListResponse {
        ContentWellnessQueryValidator.validateProgramType(programType)
        return wellnessPublicService.listPrograms(locale, programType)
    }

    @GetMapping("/programs/{documentId}")
    fun getProgram(
        @PathVariable documentId: UUID,
    ): ContentProgramResponse = wellnessPublicService.getProgram(documentId)

    @GetMapping("/programs/by-slug/{slug}")
    fun getProgramBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentProgramResponse = wellnessPublicService.getProgramBySlug(slug, locale)

    @GetMapping("/collections")
    fun listCollections(
        @RequestParam(defaultValue = "fa-IR") locale: String,
        @RequestParam(required = false) category: String?,
    ): ContentCollectionListResponse {
        ContentWellnessQueryValidator.validateCollectionCategory(category)
        return wellnessPublicService.listCollections(locale, category)
    }

    @GetMapping("/collections/{documentId}")
    fun getCollection(
        @PathVariable documentId: UUID,
    ): ContentCollectionResponse = wellnessPublicService.getCollection(documentId)

    @GetMapping("/collections/by-slug/{slug}")
    fun getCollectionBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentCollectionResponse = wellnessPublicService.getCollectionBySlug(slug, locale)

    @GetMapping("/care-schedules")
    fun listCareSchedules(
        @RequestParam(defaultValue = "fa-IR") locale: String,
        @RequestParam(required = false) gestationalWeek: Int?,
    ): ContentCareScheduleListResponse = pregnancyCompanionPublicService.listCareSchedules(locale, gestationalWeek)

    @GetMapping("/care-schedules/{documentId}")
    fun getCareSchedule(
        @PathVariable documentId: java.util.UUID,
    ): ContentCareScheduleResponse = pregnancyCompanionPublicService.getCareSchedule(documentId)

    @GetMapping("/care-schedules/by-slug/{slug}")
    fun getCareScheduleBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentCareScheduleResponse = pregnancyCompanionPublicService.getCareScheduleBySlug(slug, locale)

    @GetMapping("/checklist-templates")
    fun listChecklistTemplates(
        @RequestParam(defaultValue = "fa-IR") locale: String,
        @RequestParam(required = false) checklistKind: String?,
    ): ContentChecklistTemplateListResponse =
        pregnancyCompanionPublicService.listChecklistTemplates(locale, checklistKind)

    @GetMapping("/checklist-templates/{documentId}")
    fun getChecklistTemplate(
        @PathVariable documentId: java.util.UUID,
    ): ContentChecklistTemplateResponse = pregnancyCompanionPublicService.getChecklistTemplate(documentId)

    @GetMapping("/checklist-templates/by-slug/{slug}")
    fun getChecklistTemplateBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "fa-IR") locale: String,
    ): ContentChecklistTemplateResponse = pregnancyCompanionPublicService.getChecklistTemplateBySlug(slug, locale)
}
