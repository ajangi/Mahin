package dev.mahin.android.assistant

import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.MetaApiResponse
import dev.mahin.core.config.RemoteFeatureFlagGateway
import dev.mahin.domain.assistant.AssistantAskInput
import dev.mahin.domain.assistant.AssistantConsentScopes
import dev.mahin.domain.assistant.AssistantConsentState
import dev.mahin.domain.assistant.HealthAssistantGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantSettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun flagOffKeepsEntryHidden() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    FakeMetaApi(mapOf(MahinFeatureFlags.HEALTH_ASSISTANT to false)),
                )
            val viewModel =
                AssistantSettingsViewModel(
                    featureFlagGateway = RemoteFeatureFlagGateway(repository),
                    featureFlagRepository = repository,
                    healthAssistantGateway = NoOpAssistantGateway(),
                )
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.launchFlagEnabled)
        }

    @Test
    fun flagOnAfterRefresh() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    FakeMetaApi(mapOf(MahinFeatureFlags.HEALTH_ASSISTANT to true)),
                )
            val viewModel =
                AssistantSettingsViewModel(
                    featureFlagGateway = RemoteFeatureFlagGateway(repository),
                    featureFlagRepository = repository,
                    healthAssistantGateway = NoOpAssistantGateway(),
                )
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.launchFlagEnabled)
            assertFalse(viewModel.uiState.value.launchFlagLoading)
        }

    private class FakeMetaApi(
        private val flags: Map<String, Boolean>,
    ) : MetaApi {
        override suspend fun meta(): MetaApiResponse =
            MetaApiResponse(
                apiVersion = "0.0.1",
                environment = "test",
                featureFlags = flags,
            )
    }

    private class NoOpAssistantGateway : HealthAssistantGateway {
        override suspend fun ask(input: AssistantAskInput): Result<dev.mahin.domain.assistant.AssistantAnswer> =
            Result.failure(IllegalStateException("not used"))

        override suspend fun refreshConsent(): Result<AssistantConsentState> =
            Result.success(AssistantConsentState(scopes = AssistantConsentScopes()))

        override suspend fun updateConsent(scopes: AssistantConsentScopes): Result<AssistantConsentState> =
            Result.success(AssistantConsentState(scopes = scopes))
    }
}
