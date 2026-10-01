package dev.mahin.android.healthconnect

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.MetaApiResponse
import dev.mahin.core.config.RemoteFeatureFlagGateway
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectCoordinatorFacade
import dev.mahin.domain.healthconnect.HealthConnectAvailability
import dev.mahin.domain.healthconnect.HealthConnectClientResult
import dev.mahin.domain.healthconnect.HealthConnectRemoteClient
import dev.mahin.domain.healthconnect.HealthConnectSyncResult
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HealthConnectSettingsViewModelTest {
    @Test
    fun finishesLoadingAndEnablesWhenMetaFlagTrue() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = FeatureFlagRepository(EnabledMetaApi())
            val vm =
                HealthConnectSettingsViewModel(
                    featureFlagGateway = RemoteFeatureFlagGateway(repository),
                    featureFlagRepository = repository,
                    preferencesRepository = HealthConnectPreferencesRepository(context),
                    coordinator = NoOpCoordinator(),
                    remoteClient = StubRemoteClient(),
                )
            ShadowLooper.idleMainLooper()
            assertThat(vm.uiState.value.launchFlagLoading).isFalse()
            assertThat(vm.uiState.value.launchFlagEnabled).isTrue()
        }
    }

    @Test
    fun finishesLoadingWithFlagOffWhenMetaMissing() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = FeatureFlagRepository(EmptyMetaApi())
            val vm =
                HealthConnectSettingsViewModel(
                    featureFlagGateway = RemoteFeatureFlagGateway(repository),
                    featureFlagRepository = repository,
                    preferencesRepository = HealthConnectPreferencesRepository(context),
                    coordinator = NoOpCoordinator(),
                    remoteClient = StubRemoteClient(),
                )
            ShadowLooper.idleMainLooper()
            assertThat(vm.uiState.value.launchFlagLoading).isFalse()
            assertThat(vm.uiState.value.launchFlagEnabled).isFalse()
        }
    }

    private class EnabledMetaApi : MetaApi {
        override suspend fun meta(): MetaApiResponse =
            MetaApiResponse(
                apiVersion = "0.0.1",
                environment = "test",
                featureFlags = mapOf(MahinFeatureFlags.HEALTH_CONNECT to true),
            )
    }

    private class EmptyMetaApi : MetaApi {
        override suspend fun meta(): MetaApiResponse = MetaApiResponse(apiVersion = "0.0.1", environment = "test")
    }

    private class NoOpCoordinator : HealthConnectCoordinatorFacade {
        override fun isLaunchFlagEnabled(): Boolean = true

        override suspend fun refreshRevocationState(): HealthConnectSyncResult = HealthConnectSyncResult.Success()

        override suspend fun setUserOptIn(optIn: Boolean) = Unit

        override suspend fun importFromHealthConnect(): HealthConnectSyncResult = HealthConnectSyncResult.Success()

        override suspend fun exportToHealthConnect(): HealthConnectSyncResult = HealthConnectSyncResult.Success()
    }

    private class StubRemoteClient : HealthConnectRemoteClient {
        override suspend fun availability(): HealthConnectAvailability = HealthConnectAvailability.READY

        override suspend fun grantedPermissionStrings(): HealthConnectClientResult<Set<String>> =
            HealthConnectClientResult.Ok(emptySet())

        override suspend fun readMenstruationFlowDays():
            HealthConnectClientResult<List<dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay>> =
            HealthConnectClientResult.Ok(emptyList())

        override suspend fun upsertMenstruationFlowExports(
            exports: List<dev.mahin.domain.healthconnect.MenstruationFlowExportWrite>,
        ) = HealthConnectClientResult.Ok(0)

        override suspend fun deleteMenstruationByClientRecordIds(clientRecordIds: List<String>) =
            HealthConnectClientResult.Ok(0)
    }
}
