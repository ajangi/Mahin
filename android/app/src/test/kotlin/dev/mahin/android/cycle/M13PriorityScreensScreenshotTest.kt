package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.mahin.android.onboarding.OnboardingWelcomeScreen
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Component-level Roborazzi captures for M13 polished priority surfaces (RTL, fa-IR).
 * Golden files live under [android/app/src/test/screenshots]; review copies in
 * [docs/milestones/m13-screenshots].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR")
class M13PriorityScreensScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun todayScreen_emptyRtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    TodayScreenContent(
                        state = TodayUiState(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun logScreen_rtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    LogScreenContent(
                        state =
                            LogUiState(
                                selectedJalali = JalaliDate(1403, 6, 15),
                            ),
                        actions = noopLogScreenActionsForScreenshot(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun historyScreen_emptyRtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    HistoryScreenContent(
                        periods = emptyList(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun calendarScreen_rtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    CycleCalendarScreenContent(
                        state =
                            CalendarUiState(
                                selectedJalali = JalaliDate(1403, 6, 15),
                            ),
                        onDateSelected = {},
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .height(900.dp),
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun onboardingWelcome_rtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    OnboardingWelcomeScreen(
                        onContinue = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

private fun noopLogScreenActionsForScreenshot(): LogScreenActions =
    LogScreenActions(
        onDateSelected = {},
        onToggleLoggingPeriod = {},
        onFlowLevelSelected = {},
        onToggleSymptom = {},
        onNoteChange = {},
        onSave = {},
        onTogglePregnancySymptom = {},
        onPregnancyWeightChange = {},
        onPregnancyBpSystolicChange = {},
        onPregnancyBpDiastolicChange = {},
        ttcCallbacks =
            TtcLogFormCallbacks(
                onBbtChange = {},
                onOvulationTestSelected = {},
                onCervicalMucusSelected = {},
                onIntercourseOptInChanged = {},
                onIntercourseToggle = {},
                onIntercourseProtectedSelected = {},
                onPregnancyTestSelected = {},
            ),
    )
