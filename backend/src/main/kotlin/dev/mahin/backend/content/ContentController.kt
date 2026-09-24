package dev.mahin.backend.content

import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/v1/content")
class ContentController {
    @GetMapping("/articles/{articleId}")
    fun get(
        @PathVariable articleId: UUID,
    ): ContentArticleResponse {
        if (articleId != ENVELOPE_ID) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
        }
        return ContentArticleResponse(
            id = ENVELOPE_ID.toString(),
            slug = "m0-content-envelope",
            locale = "fa-IR",
            title = "پوستهٔ محتوا — بدون متن پزشکی",
            summary = "این رکورد فقط قرارداد CMS را نشان می‌دهد و محتوای پزشکی نیست.",
            body = null,
            contentType = "envelope",
            lifeStage = null,
            medicalRiskLevel = MedicalRiskLevel.NONE,
            status = ContentStatus.DRAFT,
            contentVersion = 1,
            sourceReferences = emptyList(),
            clinicalReviewer = null,
            clinicalReviewedAt = null,
        )
    }

    companion object {
        val ENVELOPE_ID: UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
    }
}
