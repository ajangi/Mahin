package dev.mahin.core.config

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeatureFlagRepositoryTest {
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

    @Test
    fun refreshStoresFlags() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    FakeMetaApi(mapOf(MahinFeatureFlags.HEALTH_CONNECT to true)),
                )
            assertThat(repository.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)).isFalse()
            repository.refreshFromRemote()
            assertThat(repository.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)).isTrue()
        }

    @Test
    fun gatewayReadsRepositorySnapshot() {
        val repository =
            FeatureFlagRepository(
                FakeMetaApi(emptyMap()),
            )
        val gateway = RemoteFeatureFlagGateway(repository)
        assertThat(gateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)).isFalse()
    }

    @Test
    fun healthAssistantDefaultsOffUntilRemoteRefresh() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    FakeMetaApi(mapOf(MahinFeatureFlags.HEALTH_ASSISTANT to true)),
                )
            assertThat(repository.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)).isFalse()
            repository.refreshFromRemote()
            assertThat(repository.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)).isTrue()
        }

    @Test
    fun refreshFailureKeepsPriorSnapshot() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    object : MetaApi {
                        override suspend fun meta(): MetaApiResponse = error("network down")
                    },
                )
            val result = repository.refreshFromRemote()
            assertThat(result.isFailure).isTrue()
            assertThat(repository.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)).isFalse()
        }
}
