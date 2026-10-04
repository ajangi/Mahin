package dev.mahin.core.assistant

import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.domain.assistant.AssistantAnswer
import dev.mahin.domain.assistant.AssistantAskInput
import dev.mahin.domain.assistant.AssistantConsentScopes
import dev.mahin.domain.assistant.AssistantConsentState
import dev.mahin.domain.assistant.AssistantOutcome
import dev.mahin.domain.assistant.ContentCitation
import dev.mahin.domain.assistant.HealthAssistantGateway
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

@Singleton
class RemoteHealthAssistantGateway
    @Inject
    constructor(
        private val assistantApi: AssistantApi,
        private val accountSessionRepository: AccountSessionRepository,
        private val featureFlagGateway: FeatureFlagGateway,
    ) : HealthAssistantGateway {
        override suspend fun ask(input: AssistantAskInput): Result<AssistantAnswer> {
            if (!featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)) {
                return Result.failure(AssistantDisabledException())
            }
            val token =
                accountSessionRepository.accessToken.first()
                    ?: return Result.failure(AssistantAuthRequiredException())
            val trackerContext =
                if (input.cycleSummary != null || input.symptomTags.isNotEmpty()) {
                    AssistantTrackerContextDto(
                        cycleSummary = input.cycleSummary,
                        symptomTags = input.symptomTags,
                    )
                } else {
                    null
                }
            return runCatching {
                assistantApi
                    .ask(
                        authorization = bearer(token),
                        body =
                            AssistantAskRequestDto(
                                locale = input.locale,
                                question = input.question,
                                trackerContext = trackerContext,
                            ),
                    ).toDomain()
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { error ->
                    if (error is HttpException && error.code() == 503) {
                        Result.failure(AssistantDisabledException())
                    } else {
                        Result.failure(error)
                    }
                },
            )
        }

        override suspend fun refreshConsent(): Result<AssistantConsentState> {
            if (!featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)) {
                return Result.success(AssistantConsentState())
            }
            val token =
                accountSessionRepository.accessToken.first()
                    ?: return Result.success(AssistantConsentState())
            return runCatching {
                assistantApi.getConsent(bearer(token)).toDomain()
            }
        }

        override suspend fun updateConsent(scopes: AssistantConsentScopes): Result<AssistantConsentState> {
            if (!featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)) {
                return Result.failure(AssistantDisabledException())
            }
            val token =
                accountSessionRepository.accessToken.first()
                    ?: return Result.failure(AssistantAuthRequiredException())
            return runCatching {
                assistantApi
                    .updateConsent(
                        bearer(token),
                        UpdateAssistantConsentRequestDto(
                            scopes =
                                AssistantConsentScopesDto(
                                    shareCycleSummary = scopes.shareCycleSummary,
                                    shareSymptomTags = scopes.shareSymptomTags,
                                ),
                        ),
                    ).toDomain()
            }
        }

        private fun bearer(token: String): String = "Bearer $token"

        private fun AssistantConsentResponseDto.toDomain(): AssistantConsentState =
            AssistantConsentState(
                scopes =
                    AssistantConsentScopes(
                        shareCycleSummary = scopes.shareCycleSummary,
                        shareSymptomTags = scopes.shareSymptomTags,
                    ),
                updatedAtEpochMs = updatedAtEpochMs,
            )

        private fun AssistantAskResponseDto.toDomain(): AssistantAnswer {
            val parsedOutcome =
                runCatching { AssistantOutcome.valueOf(outcome) }
                    .getOrDefault(AssistantOutcome.ERROR)
            return AssistantAnswer(
                outcome = parsedOutcome,
                answer = answer,
                citations =
                    citations.map {
                        ContentCitation(
                            documentId = it.documentId,
                            versionId = it.versionId,
                            title = it.title,
                            slug = it.slug,
                        )
                    },
                escalationCode = escalationCode,
                refusalCode = refusalCode,
                disclaimer = disclaimer,
            )
        }
    }

class AssistantDisabledException : IllegalStateException("health_assistant_disabled")

class AssistantAuthRequiredException : IllegalStateException("registered_account_required")
