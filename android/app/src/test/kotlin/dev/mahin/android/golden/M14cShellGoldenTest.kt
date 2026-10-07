package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.android.settings.SettingsScreenCallbacks
import dev.mahin.android.settings.SettingsScreenContent
import dev.mahin.android.settings.SettingsUiState
import dev.mahin.android.shell.reproductiveModeShellAccent
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.designsystem.component.MahinShellTopAppBar
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.roborazzi.MahinRoborazzi
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M14cShellGoldenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shell_cycle_light_scale10() = captureShell(ReproductiveMode.CYCLE_TRACKING, false, 1f)

    @Test
    fun shell_cycle_light_scale13() = captureShell(ReproductiveMode.CYCLE_TRACKING, false, 1.3f)

    @Test
    fun shell_cycle_dark_scale10() = captureShell(ReproductiveMode.CYCLE_TRACKING, true, 1f)

    @Test
    fun shell_cycle_dark_scale13() = captureShell(ReproductiveMode.CYCLE_TRACKING, true, 1.3f)

    @Test
    fun shell_ttc_light_scale10() = captureShell(ReproductiveMode.TRYING_TO_CONCEIVE, false, 1f)

    @Test
    fun shell_ttc_light_scale13() = captureShell(ReproductiveMode.TRYING_TO_CONCEIVE, false, 1.3f)

    @Test
    fun shell_ttc_dark_scale10() = captureShell(ReproductiveMode.TRYING_TO_CONCEIVE, true, 1f)

    @Test
    fun shell_ttc_dark_scale13() = captureShell(ReproductiveMode.TRYING_TO_CONCEIVE, true, 1.3f)

    @Test
    fun shell_pregnant_light_scale10() = captureShell(ReproductiveMode.PREGNANT, false, 1f)

    @Test
    fun shell_pregnant_light_scale13() = captureShell(ReproductiveMode.PREGNANT, false, 1.3f)

    @Test
    fun shell_pregnant_dark_scale10() = captureShell(ReproductiveMode.PREGNANT, true, 1f)

    @Test
    fun shell_pregnant_dark_scale13() = captureShell(ReproductiveMode.PREGNANT, true, 1.3f)

    @Test
    fun shell_postPregnancy_light_scale10() = captureShell(ReproductiveMode.POST_PREGNANCY_TRANSITION, false, 1f)

    @Test
    fun shell_postPregnancy_light_scale13() = captureShell(ReproductiveMode.POST_PREGNANCY_TRANSITION, false, 1.3f)

    @Test
    fun shell_postPregnancy_dark_scale10() = captureShell(ReproductiveMode.POST_PREGNANCY_TRANSITION, true, 1f)

    @Test
    fun shell_postPregnancy_dark_scale13() = captureShell(ReproductiveMode.POST_PREGNANCY_TRANSITION, true, 1.3f)

    @Test
    fun shell_paused_light_scale10() = captureShell(ReproductiveMode.TRACKING_PAUSED, false, 1f)

    @Test
    fun shell_paused_light_scale13() = captureShell(ReproductiveMode.TRACKING_PAUSED, false, 1.3f)

    @Test
    fun shell_paused_dark_scale10() = captureShell(ReproductiveMode.TRACKING_PAUSED, true, 1f)

    @Test
    fun shell_paused_dark_scale13() = captureShell(ReproductiveMode.TRACKING_PAUSED, true, 1.3f)

    @Test
    fun settings_populated_light_scale10() = captureSettings(false, 1f)

    @Test
    fun settings_populated_light_scale13() = captureSettings(false, 1.3f)

    @Test
    fun settings_populated_dark_scale10() = captureSettings(true, 1f)

    @Test
    fun settings_populated_dark_scale13() = captureSettings(true, 1.3f)

    @Test
    fun settings_assistantOn_light_scale10() = captureSettings(false, 1f, assistantOn = true)

    @Test
    fun settings_assistantOn_light_scale13() = captureSettings(false, 1.3f, assistantOn = true)

    private fun captureShell(
        mode: ReproductiveMode,
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        val destinations = MahinTopLevelDestination.forMode(mode)
        val selected = destinations.first()
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            Scaffold(
                topBar = {
                    MahinShellTopAppBar(
                        title = stringResource(selected.labelRes),
                        onOpenSettings = {},
                    )
                },
                bottomBar = {
                    MahinBottomNavigationBar(
                        tabs = destinations.map { it.toNavTab() },
                        selectedRoute = selected.route,
                        modeAccent = reproductiveModeShellAccent(mode),
                        onTabSelected = {},
                    )
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    Text(text = stringResource(selected.labelRes))
                }
            }
        }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun captureSettings(
        darkTheme: Boolean,
        fontScale: Float,
        assistantOn: Boolean = false,
    ) {
        if (assistantOn) {
            composeRule.setContent {
                val baseDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalDensity provides Density(density = baseDensity.density, fontScale = fontScale),
                ) {
                    MahinTheme(darkTheme = darkTheme) {
                        Box(Modifier.fillMaxSize()) {
                            SettingsGoldenContent(assistantOn = true)
                        }
                    }
                }
            }
            composeRule.waitForIdle()
            composeRule
                .onNodeWithTag("settings_screen_list")
                .performScrollToNode(hasText("دستیار آموزشی (آزمایشی)"))
            composeRule.waitForIdle()
            composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
        } else {
            composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
                Box(Modifier.fillMaxSize()) {
                    SettingsGoldenContent(assistantOn = false)
                }
            }
        }
    }

    @Composable
    private fun SettingsGoldenContent(assistantOn: Boolean) {
        SettingsScreenContent(
            state =
                SettingsUiState(
                    healthConnectEntryVisible = true,
                    healthAssistantEntryVisible = assistantOn,
                ),
            callbacks =
                SettingsScreenCallbacks(
                    onNavigateUp = {},
                    onModeSelected = {},
                    onOpenNotifications = {},
                    onOpenPrivacy = {},
                    onOpenHealthConnect = {},
                    onOpenAssistant = {},
                    onOpenDataExport = {},
                    onOpenPremium = {},
                    onOpenHistory = {},
                    onResumeCycle = {},
                    onResumeTtc = {},
                ),
        )
    }
}
