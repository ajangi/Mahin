package dev.mahin.backend.cms

import dev.mahin.backend.content.ContentAdminService
import dev.mahin.backend.content.ContentSourceResponse
import dev.mahin.backend.content.CreateContentSourceRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/content/sources")
class CmsSourceController(
    private val contentAdminService: ContentAdminService,
) {
    @GetMapping
    fun list(): List<ContentSourceResponse> = contentAdminService.listSources()

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateContentSourceRequest,
    ): ContentSourceResponse = contentAdminService.createSource(request)
}
