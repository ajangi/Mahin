package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.android.cycle.editor.DayEditorManageCategoriesPanel
import dev.mahin.android.cycle.editor.DayEditorSheetContent
import dev.mahin.android.cycle.editor.DayEditorTestHarness
import dev.mahin.android.premium.PremiumBillingCoordinator
import dev.mahin.android.settings.SettingsScreenCallbacks
import dev.mahin.android.settings.SettingsScreenContent
import dev.mahin.android.settings.SettingsViewModel
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.BillingAdapterState
import dev.mahin.core.billing.BillingPurchaseReceipt
import dev.mahin.core.billing.EntitlementApi
import dev.mahin.core.billing.EntitlementApiResponse
import dev.mahin.core.billing.EntitlementRepository
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.MetaApiResponse
import dev.mahin.core.config.RemoteFeatureFlagGateway
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class LoggingLifestyleEnableTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var harness: DayEditorTestHarness
    private val settingsStore = ViewModelStoreTestHarness()
    private val day = LocalDate.of(2025, 1, 1)

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
    }

    @After
    fun tearDown() {
        settingsStore.clear()
        harness.close()
    }

    @Test
    fun dayEditor_sleepHiddenUntilEnabledFromManage() {
        val editorVm = harness.createViewModel()
        runBlocking { editorVm.openDate(day) }
        androidComposeRule.setContent {
            val state by editorVm.uiState.collectAsState()
            MahinTheme {
                if (state.lifestyleEnabled[LogEditorCategory.SLEEP] != true) {
                    DayEditorManageCategoriesPanel(
                        state = state,
                        onLifestyleToggle = editorVm::setLifestyleEnabled,
                        onRequestSexualOptIn = {},
                    )
                } else {
                    DayEditorSheetContent(date = day, state = state)
                }
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_switch").performClick()
        waitForSleepEnabled(editorVm)
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_toggle").assertExists()
    }

    @Test
    fun settingsToggle_enablesSleepInEditor() {
        val settingsVm = createSettingsViewModel()
        val editorVm = harness.createViewModel()
        runBlocking { editorVm.openDate(day) }
        androidComposeRule.setContent {
            val settingsState by settingsVm.uiState.collectAsState()
            val editorState by editorVm.uiState.collectAsState()
            MahinTheme {
                if (editorState.lifestyleEnabled[LogEditorCategory.SLEEP] != true) {
                    SettingsScreenContent(
                        state = settingsState,
                        callbacks =
                            SettingsScreenCallbacks(
                                onNavigateUp = {},
                                onModeSelected = settingsVm::onReproductiveModeSelected,
                                onOpenNotifications = {},
                                onOpenPrivacy = {},
                                onOpenHealthConnect = {},
                                onOpenAssistant = {},
                                onOpenDataExport = {},
                                onOpenPremium = {},
                                onOpenHistory = {},
                                onResumeCycle = settingsVm::resumeCycleTracking,
                                onResumeTtc = settingsVm::resumeTtc,
                                onLifestyleModuleToggle = settingsVm::setLifestyleModuleEnabled,
                            ),
                    )
                } else {
                    DayEditorSheetContent(
                        date = day,
                        state = editorState.copy(orderedCategories = listOf(LogEditorCategory.SLEEP)),
                    )
                }
            }
        }
        androidComposeRule
            .onNodeWithTag("settings_screen_list")
            .performScrollToNode(hasTestTag("settings_lifestyle_sleep"))
        androidComposeRule.onNodeWithTag("settings_lifestyle_sleep_switch").performClick()
        waitForSleepEnabled(editorVm)
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_toggle").assertExists()
    }

    private fun waitForSleepEnabled(editorVm: dev.mahin.android.cycle.editor.DayEditorViewModel) {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            if (editorVm.uiState.value.lifestyleEnabled[LogEditorCategory.SLEEP] == true) {
                androidComposeRule.waitForIdle()
                return
            }
            androidComposeRule.waitForIdle()
        }
        throw AssertionError("Sleep module not enabled")
    }

    private fun createSettingsViewModel(): SettingsViewModel {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<android.content.Context>()
        val flags = mapOf(MahinFeatureFlags.HEALTH_CONNECT to true, MahinFeatureFlags.HEALTH_ASSISTANT to false)
        val repository = FeatureFlagRepository(TestMetaApi(flags))
        val entitlementRepository =
            EntitlementRepository(
                SubscriptionPreferencesRepository(context),
                AccountSessionRepository(context),
                TestEntitlementApi(),
            )
        val billing = NoOpBillingAdapter()
        return settingsStore.hold(
            SettingsViewModel(
                pregnancyRepository =
                    PregnancyTrackingRepository(
                        harness.database,
                        PregnancyTimerPreferencesRepository(context),
                    ),
                featureFlagGateway = RemoteFeatureFlagGateway(repository),
                featureFlagRepository = repository,
                billingAdapter = billing,
                premiumBillingCoordinator = PremiumBillingCoordinator(billing, entitlementRepository),
                entitlementRepository = entitlementRepository,
                loggingPreferencesRepository = harness.loggingPreferencesRepository,
                localDateClock = FixedLocalDateClock(day),
            ),
        )
    }

    private class TestMetaApi(
        private val flags: Map<String, Boolean>,
    ) : MetaApi {
        override suspend fun meta(): MetaApiResponse =
            MetaApiResponse(apiVersion = "v1", environment = "test", featureFlags = flags)
    }

    private class TestEntitlementApi : EntitlementApi {
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

    private class NoOpBillingAdapter : BillingAdapter {
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
