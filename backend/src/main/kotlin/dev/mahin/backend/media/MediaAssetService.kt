package dev.mahin.backend.media

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class MediaAssetService(
    private val urlFactory: MediaPublicUrlFactory,
) {
    private val placeholder = MediaAssetResponse(
        id = PLACEHOLDER_ID.toString(),
        storageKey = "placeholders/non-medical/foundation-mark/v1",
        family = MediaFamily.non_medical_placeholder,
        version = 1,
        locale = "fa-IR",
        publicUrl = "",
        medicalGoverned = false,
        approvalStatus = MediaApprovalStatus.draft,
        sourceReferences = emptyList(),
        reviewer = null,
        reviewedAt = null,
        altText = "نشان تزئینی غیرپزشکی برای زیرساخت مهندسی",
        createdAt = Instant.parse("2026-01-01T00:00:00Z").toString(),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z").toString(),
        supersededBy = null,
    )

    fun get(id: UUID): MediaAssetResponse {
        if (id != PLACEHOLDER_ID) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "media_not_found")
        }
        return placeholder.copy(publicUrl = urlFactory.urlFor(placeholder.storageKey))
    }

    companion object {
        val PLACEHOLDER_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
    }
}
