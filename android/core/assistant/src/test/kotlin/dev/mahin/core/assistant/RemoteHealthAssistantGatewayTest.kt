package dev.mahin.core.assistant

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.domain.assistant.AssistantAskInput
import dev.mahin.domain.assistant.AssistantOutcome
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.HttpException
import retrofit2.Response

class RemoteHealthAssistantGatewayTest {
    @Test
    fun askDoesNotCallApiWhenKillSwitchOff() =
        runTest {
            val api = FakeAssistantApi()
            val gateway = gateway(api, flagsEnabled = false, token = "token")
            val result = gateway.ask(AssistantAskInput(question = "test"))
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(AssistantDisabledException::class.java)
            assertThat(api.askCalled).isFalse()
        }

    @Test
    fun maps503ToDisabled() =
        runTest {
            val api =
                FakeAssistantApi().apply {
                    nextAskThrowable =
                        HttpException(
                            Response.error<Any>(
                                503,
                                "".toResponseBody("application/json".toMediaType()),
                            ),
                        )
                }
            val gateway = gateway(api, flagsEnabled = true, token = "token")
            val result = gateway.ask(AssistantAskInput(question = "test"))
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(AssistantDisabledException::class.java)
        }

    @Test
    fun unknownOutcomeMapsToError() =
        runTest {
            val api =
                FakeAssistantApi().apply {
                    nextAskResponse =
                        AssistantAskResponseDto(
                            outcome = "NOT_A_REAL_OUTCOME",
                            disclaimer = "x",
                        )
                }
            val gateway = gateway(api, flagsEnabled = true, token = "token")
            val result = gateway.ask(AssistantAskInput(question = "test"))
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()?.outcome).isEqualTo(AssistantOutcome.ERROR)
        }

    private fun gateway(
        api: AssistantApi,
        flagsEnabled: Boolean,
        token: String?,
    ): RemoteHealthAssistantGateway {
        val session = mock(AccountSessionRepository::class.java)
        `when`(session.accessToken).thenReturn(flowOf(token))
        return RemoteHealthAssistantGateway(
            assistantApi = api,
            accountSessionRepository = session,
            featureFlagGateway =
                object : FeatureFlagGateway {
                    override fun isEnabled(flag: String): Boolean =
                        flagsEnabled && flag == MahinFeatureFlags.HEALTH_ASSISTANT
                },
        )
    }

    private class FakeAssistantApi : AssistantApi {
        var askCalled = false
        var nextAskResponse: AssistantAskResponseDto? = null
        var nextAskThrowable: Throwable? = null

        override suspend fun getConsent(authorization: String): AssistantConsentResponseDto = error("not used")

        override suspend fun updateConsent(
            authorization: String,
            body: UpdateAssistantConsentRequestDto,
        ): AssistantConsentResponseDto = error("not used")

        override suspend fun ask(
            authorization: String,
            body: AssistantAskRequestDto,
        ): AssistantAskResponseDto {
            askCalled = true
            nextAskThrowable?.let { throw it }
            return nextAskResponse ?: error("missing response")
        }
    }
}
