package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentCatalogStateEntity
import dev.mahin.backend.content.persistence.ContentCatalogStateRepository
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContentCatalogService(
    private val catalogStateRepository: ContentCatalogStateRepository,
) {
    fun currentStatus(): ContentCatalogStatusResponse {
        val state = catalogStateRepository.findById(1).orElseGet { bootstrap() }
        return ContentCatalogStatusResponse(
            publicationRevision = state.publicationRevision,
            updatedAt = DateTimeFormatter.ISO_INSTANT.format(state.updatedAt),
        )
    }

    @Transactional
    fun bumpPublicationRevision(): Long {
        val state = catalogStateRepository.findById(1).orElseGet { bootstrap() }
        state.publicationRevision += 1
        state.updatedAt = Instant.now()
        catalogStateRepository.save(state)
        return state.publicationRevision
    }

    private fun bootstrap(): ContentCatalogStateEntity {
        val entity = ContentCatalogStateEntity()
        return catalogStateRepository.save(entity)
    }
}
