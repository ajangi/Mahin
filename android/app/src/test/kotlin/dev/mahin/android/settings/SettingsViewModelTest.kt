package dev.mahin.android.settings

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
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
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.core.datastore.CachedEntitlement
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.domain.subscription.EntitlementTier
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private lateinit var context: Context
    private val viewModelStore = ViewModelStoreTestHarness()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
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
        viewModelStore.clear()
        runBlocking {
            SubscriptionPreferencesRepository(context).clear()
        }
        database.close()
    }

    private fun idle() {
        ShadowLooper.idleMainLooper()
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 2_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
            delay(25)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }

    @Test
    fun featureFlags_allFourCombinations() {
        val combinations =
            listOf(
                Pair(true, true),
                Pair(true, false),
                Pair(false, true),
                Pair(false, false),
            )
        combinations.forEach { (healthConnect, assistant) ->
            val viewModel =
                createViewModel(
                    flags =
                        mapOf(
                            MahinFeatureFlags.HEALTH_CONNECT to healthConnect,
                            MahinFeatureFlags.HEALTH_ASSISTANT to assistant,
                        ),
                )
            idle()
            val state = viewModel.uiState.value
            assertThat(state.healthConnectEntryVisible).isEqualTo(healthConnect)
            assertThat(state.healthAssistantEntryVisible).isEqualTo(assistant)
        }
    }

    @Test
    fun pregnantModeWithoutActivePregnancy_opensStartSheet() {
        runBlocking { seedProfile(ReproductiveMode.CYCLE_TRACKING) }
        val viewModel = createViewModel()
        idle()
        viewModel.onReproductiveModeSelected(ReproductiveMode.PREGNANT)
        idle()
        assertTrue(viewModel.uiState.value.showPregnancyStartSheet)
    }

    @Test
    fun modeChangeBlockedDuringActivePregnancy() {
        runBlocking {
            seedProfile(ReproductiveMode.CYCLE_TRACKING)
            pregnancyRepository.startPregnancy(
                lmpDate = LocalDate.of(2025, 1, 1),
                clinicalEddDate = null,
                datingReason = null,
            )
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.hasActivePregnancy }
            viewModel.onReproductiveModeSelected(ReproductiveMode.CYCLE_TRACKING)
            awaitUntil { viewModel.uiState.value.modeChangeBlockedMessage }
        }
    }

    @Test
    fun selectingTtc_updatesRepositoryMode() {
        runBlocking {
            seedProfile(ReproductiveMode.CYCLE_TRACKING)
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.CYCLE_TRACKING }
            viewModel.onReproductiveModeSelected(ReproductiveMode.TRYING_TO_CONCEIVE)
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
        }
    }

    @Test
    fun selectingCycle_updatesRepositoryMode() {
        runBlocking {
            seedProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
            viewModel.onReproductiveModeSelected(ReproductiveMode.CYCLE_TRACKING)
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.CYCLE_TRACKING }
        }
    }

    @Test
    fun paywall_twoOpensWhileVisible_startConnectionOnce() {
        val billingAdapter = CountingBillingAdapter()
        val viewModel = createViewModel(billingAdapter = billingAdapter)
        viewModel.openPaywall()
        viewModel.openPaywall()
        idle()
        assertThat(billingAdapter.startConnectionCount).isEqualTo(1)
    }

    @Test
    fun paywall_dismissDuringWarmUp_canReopen() {
        runBlocking {
            val billingAdapter = SlowBillingAdapter()
            val viewModel = createViewModel(billingAdapter = billingAdapter)
            viewModel.openPaywall()
            viewModel.dismissPaywall()
            viewModel.openPaywall()
            awaitUntil { viewModel.uiState.value.showPaywall }
        }
    }

    @Test
    fun paywall_dismiss_recordsCancellationExceptionInBillingAdapter() {
        val billingAdapter = CancellableWarmUpBillingAdapter()
        val viewModel = createViewModel(billingAdapter = billingAdapter)
        viewModel.openPaywall()
        idle()
        assertTrue(viewModel.uiState.value.showPaywall)
        viewModel.dismissPaywall()
        idle()
        assertFalse(viewModel.uiState.value.showPaywall)
        assertThat(billingAdapter.cancellationCause).isInstanceOf(CancellationException::class.java)
    }

    @Test
    fun paywall_throwingWarmUp_stillAllowsReopen() {
        runBlocking {
            val billingAdapter = ThrowingBillingAdapter()
            val viewModel = createViewModel(billingAdapter = billingAdapter)
            viewModel.openPaywall()
            awaitUntil { billingAdapter.startConnectionCount == 1 }
            viewModel.dismissPaywall()
            viewModel.openPaywall()
            awaitUntil { viewModel.uiState.value.showPaywall }
        }
    }

    @Test
    fun paywall_secondOpenAfterCancel_startsWarmUpAgain() {
        runBlocking {
            val billingAdapter = SlowBillingAdapter()
            val viewModel = createViewModel(billingAdapter = billingAdapter)
            viewModel.openPaywall()
            awaitUntil(timeoutMs = 5_000) { billingAdapter.startConnectionCount == 1 }
            viewModel.dismissPaywall()
            idle()
            viewModel.openPaywall()
            awaitUntil(timeoutMs = 5_000) { billingAdapter.startConnectionCount == 2 }
        }
    }

    @Test
    fun paywall_openAndDismiss() {
        val billingAdapter = CountingBillingAdapter()
        val viewModel = createViewModel(billingAdapter = billingAdapter)
        idle()
        assertFalse(viewModel.uiState.value.showPaywall)
        assertThat(billingAdapter.startConnectionCount).isEqualTo(0)
        viewModel.openPaywall()
        assertTrue(viewModel.uiState.value.showPaywall)
        idle()
        assertThat(billingAdapter.startConnectionCount).isEqualTo(1)
        viewModel.dismissPaywall()
        idle()
        assertFalse(viewModel.uiState.value.showPaywall)
    }

    @Test
    fun resumeTtc_updatesRepositoryMode() {
        runBlocking {
            seedProfile(ReproductiveMode.POST_PREGNANCY_TRANSITION)
            val viewModel = createViewModel()
            awaitUntil {
                viewModel.uiState.value.reproductiveMode == ReproductiveMode.POST_PREGNANCY_TRANSITION
            }
            viewModel.resumeTtc()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
        }
    }

    @Test
    fun premiumEntitlement_hidesPaywallEntry() {
        runBlocking {
            SubscriptionPreferencesRepository(context).saveEntitlement(
                CachedEntitlement(
                    tierName = EntitlementTier.PREMIUM_MONTHLY.name,
                    expiresAtEpochMs = null,
                    sourceName = SubscriptionPreferencesRepository.SOURCE_GOOGLE_PLAY,
                    syncedAtEpochMs = null,
                ),
            )
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.premiumActive }
            assertFalse(viewModel.uiState.value.showPremiumPaywallEntry)
        }
    }

    private suspend fun seedProfile(mode: ReproductiveMode) {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = mode,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.UNKNOWN,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    private fun createViewModel(
        flags: Map<String, Boolean> =
            mapOf(
                MahinFeatureFlags.HEALTH_CONNECT to true,
                MahinFeatureFlags.HEALTH_ASSISTANT to false,
            ),
        billingAdapter: BillingAdapter = CountingBillingAdapter(),
        entitlementRepository: EntitlementRepository =
            EntitlementRepository(
                SubscriptionPreferencesRepository(context),
                AccountSessionRepository(context),
                FakeEntitlementApi(),
            ),
    ): SettingsViewModel {
        val repository = FeatureFlagRepository(FakeMetaApi(flags))
        val gateway = RemoteFeatureFlagGateway(repository)
        val premiumCoordinator = PremiumBillingCoordinator(billingAdapter, entitlementRepository)
        return viewModelStore.hold(
            SettingsViewModel(
                pregnancyRepository = pregnancyRepository,
                featureFlagGateway = gateway,
                featureFlagRepository = repository,
                billingAdapter = billingAdapter,
                premiumBillingCoordinator = premiumCoordinator,
                entitlementRepository = entitlementRepository,
            ),
        )
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

    private open class CountingBillingAdapter : BillingAdapter {
        override val state = MutableStateFlow<BillingAdapterState>(BillingAdapterState.Idle)
        var startConnectionCount = 0

        override suspend fun startConnection() {
            startConnectionCount++
        }

        override suspend fun refreshProducts() = Unit

        override suspend fun restorePurchases(): List<BillingPurchaseReceipt> = emptyList()

        override suspend fun launchSubscriptionPurchase(
            activityHost: Any,
            productId: String,
        ): BillingPurchaseReceipt? = null
    }

    private class SlowBillingAdapter : CountingBillingAdapter() {
        override suspend fun startConnection() {
            startConnectionCount++
            delay(500)
        }
    }

    private class CancellableWarmUpBillingAdapter : CountingBillingAdapter() {
        var cancellationCause: CancellationException? = null

        override suspend fun startConnection() {
            try {
                delay(Long.MAX_VALUE)
            } catch (cancelled: CancellationException) {
                cancellationCause = cancelled
                throw cancelled
            }
        }
    }

    private class ThrowingBillingAdapter : CountingBillingAdapter() {
        override suspend fun startConnection() {
            startConnectionCount++
            error("billing warm-up failed")
        }
    }
}
