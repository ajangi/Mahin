package dev.mahin.backend.assistant

import dev.mahin.backend.assistant.persistence.AssistantConsentEntity
import dev.mahin.backend.assistant.persistence.AssistantConsentRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssistantConsentService(
    private val consentRepository: AssistantConsentRepository,
) {
    fun getConsent(userId: UUID): AssistantConsentResponse {
        val entity = consentRepository.findById(userId).orElse(null)
        return if (entity == null) {
            AssistantConsentResponse(
                scopes = AssistantConsentScopes(),
                updatedAtEpochMs = null,
            )
        } else {
            AssistantConsentResponse(
                scopes =
                    AssistantConsentScopes(
                        shareCycleSummary = entity.shareCycleSummary,
                        shareSymptomTags = entity.shareSymptomTags,
                    ),
                updatedAtEpochMs = entity.updatedAt.toEpochMilli(),
            )
        }
    }

    @Transactional
    fun updateConsent(
        userId: UUID,
        request: UpdateAssistantConsentRequest,
    ): AssistantConsentResponse {
        val now = Instant.now()
        val entity =
            consentRepository.findById(userId).orElse(
                AssistantConsentEntity(userId = userId),
            )
        entity.shareCycleSummary = request.scopes.shareCycleSummary
        entity.shareSymptomTags = request.scopes.shareSymptomTags
        entity.updatedAt = now
        consentRepository.save(entity)
        return AssistantConsentResponse(
            scopes = request.scopes,
            updatedAtEpochMs = now.toEpochMilli(),
        )
    }
}
