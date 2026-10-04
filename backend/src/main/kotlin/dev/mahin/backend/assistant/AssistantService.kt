package dev.mahin.backend.assistant

import dev.mahin.backend.config.MahinFeatureFlagsProperties
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
@Suppress("LongParameterList")
class AssistantService(
    private val featureFlags: MahinFeatureFlagsProperties,
    private val properties: AssistantProperties,
    private val consentService: AssistantConsentService,
    private val retriever: CompositeApprovedContentRetriever,
    private val contextRedactor: AssistantContextRedactor,
    private val gateway: HealthAssistantGateway,
    private val auditLogger: AssistantAuditLogger,
) {
    fun getConsent(userId: UUID): AssistantConsentResponse = consentService.getConsent(userId)

    fun updateConsent(
        userId: UUID,
        request: UpdateAssistantConsentRequest,
    ): AssistantConsentResponse = consentService.updateConsent(userId, request)

    fun ask(
        userId: UUID,
        request: AssistantAskRequest,
    ): AssistantAskResponse {
        ensureAssistantEnabled()
        val consent = consentService.getConsent(userId).scopes
        val retrieved = retriever.retrieve(request.question, request.locale)
        val escalation = AssistantEscalationEngine.evaluate(request.question, retrieved)
        val blocked = escalationResponse(userId, escalation)
        if (blocked != null) {
            return blocked
        }

        val redactedContext = contextRedactor.redact(request.trackerContext, consent)
        val gatewayResponse =
            gateway.ask(
                AssistantGatewayRequest(
                    locale = request.locale,
                    question = request.question,
                    redactedContext = redactedContext,
                    retrievedChunks = retrieved,
                    promptTemplateId = properties.promptTemplateId,
                ),
            )
        auditLogger.record(
            AssistantInteractionMetadata(
                userId = userId,
                promptTemplateId = properties.promptTemplateId,
                providerId = gatewayResponse.providerId,
                modelVersion = gatewayResponse.modelVersion,
                outcomeClass = AssistantOutcomeClass.ANSWERED,
            ),
        )
        return AssistantAskResponse(
            outcome = AssistantOutcomeClass.ANSWERED,
            answer = gatewayResponse.answer,
            citations = gatewayResponse.citations,
        )
    }

    private fun escalationResponse(
        userId: UUID,
        escalation: AssistantEscalationEngine.Result,
    ): AssistantAskResponse? =
        when (escalation.decision) {
            AssistantEscalationEngine.Decision.ESCALATE_CRISIS,
            AssistantEscalationEngine.Decision.ESCALATE_URGENT,
            -> {
                auditLogger.record(
                    AssistantInteractionMetadata(
                        userId = userId,
                        promptTemplateId = properties.promptTemplateId,
                        providerId = properties.provider,
                        modelVersion = "n/a",
                        outcomeClass = AssistantOutcomeClass.ESCALATED,
                    ),
                )
                AssistantAskResponse(
                    outcome = AssistantOutcomeClass.ESCALATED,
                    escalationCode = escalation.code,
                    answer = null,
                )
            }
            AssistantEscalationEngine.Decision.REFUSE_DIAGNOSIS,
            AssistantEscalationEngine.Decision.REFUSE_PRESCRIPTION,
            AssistantEscalationEngine.Decision.REFUSE_NO_GROUNDING,
            -> {
                auditLogger.record(
                    AssistantInteractionMetadata(
                        userId = userId,
                        promptTemplateId = properties.promptTemplateId,
                        providerId = properties.provider,
                        modelVersion = "n/a",
                        outcomeClass = AssistantOutcomeClass.REFUSED,
                    ),
                )
                AssistantAskResponse(
                    outcome = AssistantOutcomeClass.REFUSED,
                    refusalCode = escalation.code,
                    answer = null,
                )
            }
            AssistantEscalationEngine.Decision.PROCEED -> null
        }

    private fun ensureAssistantEnabled() {
        if (!featureFlags.healthAssistant) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "assistant_disabled")
        }
        if (!properties.isProviderConfigured()) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "assistant_not_configured")
        }
    }
}
