package dev.mahin.backend.media

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/media")
class MediaController(
    private val mediaAssetService: MediaAssetService,
) {
    @GetMapping("/assets/{assetId}")
    fun get(@PathVariable assetId: UUID): MediaAssetResponse = mediaAssetService.get(assetId)
}
