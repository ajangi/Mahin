package dev.mahin.android.settings

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.premium.PremiumBillingCoordinator
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.BillingAdapterState
import dev.mahin.core.billing.BillingPurchaseReceipt
import dev.mahin.core.billing.EntitlementApi
import dev.mahin.core.billing.EntitlementApiResponse
import dev.mahin.core.billing.EntitlementRepository
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.MetaApiResponse
import dev.mahin.core.config.RemoteFeatureFlagGateway
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: MahinDatabase
    private lateinit var pregnancyRepository: PregnancyTrackingRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        pregnancyRepository =
            PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun featureFlags_gateHealthConnectAndAssistant() =
        runTest {
            val repository =
                FeatureFlagRepository(
                    FakeMetaApi(
                        mapOf(
                            MahinFeatureFlags.HEALTH_CONNECT to true,
                            MahinFeatureFlags.HEALTH_ASSISTANT to false,
                        ),
                    ),
                )
            val gateway = RemoteFeatureFlagGateway(repository)
            val context = ApplicationProvider.getApplicationContext<Context>()
            val billingAdapter = IdleBillingAdapter()
            val premiumCoordinator =
                PremiumBillingCoordinator(
                    billingAdapter,
                    EntitlementRepository(
                        SubscriptionPreferencesRepository(context),
                        AccountSessionRepository(context),
                        FakeEntitlementApi(),
                    ),
                )
            val viewModel =
                SettingsViewModel(
                    pregnancyRepository = pregnancyRepository,
                    featureFlagGateway = gateway,
                    featureFlagRepository = repository,
                    billingAdapter = billingAdapter,
                    premiumBillingCoordinator = premiumCoordinator,
                )
            advanceUntilIdle()
            val state = viewModel.uiState.value
            assertTrue(state.healthConnectEntryVisible)
            assertFalse(state.healthAssistantEntryVisible)
        }

    private class FakeMetaApi(
        private val flags: Map<String, Boolean>,
    ) : MetaApi {
        override suspend fun meta(): MetaApiResponse =
            MetaApiResponse(
                apiVersion = "v1",
                environment = "test",
                featureFlags = flags,
            )
    }

    private class FakeEntitlementApi : EntitlementApi {
        override suspend fun currentEntitlement(authorization: String): EntitlementApiResponse =
            EntitlementApiResponse("FREE", null, emptyList(), "2026-01-01T00:00:00Z")

        override suspend fun verifyPurchase(
            authorization: String,
            body: dev.mahin.core.billing.GooglePlayVerifyRequest,
        ): dev.mahin.core.billing.GooglePlayBillingResponse =
            dev.mahin.core.billing
                .GooglePlayBillingResponse("FREE", null)

        override suspend fun restorePurchases(
            authorization: String,
            body: dev.mahin.core.billing.GooglePlayRestoreRequest,
        ): dev.mahin.core.billing.GooglePlayBillingResponse =
            dev.mahin.core.billing
                .GooglePlayBillingResponse("FREE", null)
    }

    private class IdleBillingAdapter : BillingAdapter {
        override val state = MutableStateFlow<BillingAdapterState>(BillingAdapterState.Idle)

        override suspend fun startConnection() = Unit

        override suspend fun refreshProducts() = Unit

        override suspend fun restorePurchases(): List<BillingPurchaseReceipt> = emptyList()

        override suspend fun launchSubscriptionPurchase(
            activityHost: Any,
            productId: String,
        ): BillingPurchaseReceipt? = null
    }
}
