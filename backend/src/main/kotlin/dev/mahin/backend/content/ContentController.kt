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
}
