package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.mahin.android.cycle.CycleCalendarScreenContent
import dev.mahin.android.cycle.HistoryScreenContent
import dev.mahin.android.cycle.LogScreenContent
import dev.mahin.android.cycle.TodayScreenContent
import dev.mahin.android.insights.CycleInsightsScreenContent
import dev.mahin.android.learn.LearnScreenContent
import dev.mahin.android.onboarding.OnboardingWelcomeScreen
import dev.mahin.android.pregnancy.PregnancyHubScreenContent
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Full-screen V2 golden harness (fa-IR RTL, light/dark, font scale 1.0 / 1.3).
 * Goldens: [android/app/src/test/screenshots].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M14aFullScreenGoldenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun today_populated_light_scale10() = captureToday(darkTheme = false, fontScale = 1f)

    @Test
    fun today_populated_light_scale13() = captureToday(darkTheme = false, fontScale = 1.3f)

    @Test
    fun today_populated_dark_scale10() = captureToday(darkTheme = true, fontScale = 1f)

    @Test
    fun today_populated_dark_scale13() = captureToday(darkTheme = true, fontScale = 1.3f)

    @Test
    fun calendar_populated_light_scale10() = captureCalendar(darkTheme = false, fontScale = 1f)

    @Test
    fun calendar_populated_light_scale13() = captureCalendar(darkTheme = false, fontScale = 1.3f)

    @Test
    fun calendar_populated_dark_scale10() = captureCalendar(darkTheme = true, fontScale = 1f)

    @Test
    fun calendar_populated_dark_scale13() = captureCalendar(darkTheme = true, fontScale = 1.3f)

    @Test
    fun log_populated_light_scale10() = captureLog(darkTheme = false, fontScale = 1f)

    @Test
    fun log_populated_light_scale13() = captureLog(darkTheme = false, fontScale = 1.3f)

    @Test
    fun log_populated_dark_scale10() = captureLog(darkTheme = true, fontScale = 1f)

    @Test
    fun log_populated_dark_scale13() = captureLog(darkTheme = true, fontScale = 1.3f)

    @Test
    fun history_populated_light_scale10() = captureHistory(darkTheme = false, fontScale = 1f)

    @Test
    fun history_populated_light_scale13() = captureHistory(darkTheme = false, fontScale = 1.3f)

    @Test
    fun history_populated_dark_scale10() = captureHistory(darkTheme = true, fontScale = 1f)

    @Test
    fun history_populated_dark_scale13() = captureHistory(darkTheme = true, fontScale = 1.3f)

    @Test
    fun onboardingWelcome_populated_light_scale10() = captureOnboarding(darkTheme = false, fontScale = 1f)

    @Test
    fun onboardingWelcome_populated_light_scale13() = captureOnboarding(darkTheme = false, fontScale = 1.3f)

    @Test
    fun onboardingWelcome_populated_dark_scale10() = captureOnboarding(darkTheme = true, fontScale = 1f)

    @Test
    fun onboardingWelcome_populated_dark_scale13() = captureOnboarding(darkTheme = true, fontScale = 1.3f)

    @Test
    fun cycleInsights_populated_light_scale10() = captureCycleInsights(darkTheme = false, fontScale = 1f)

    @Test
    fun cycleInsights_populated_light_scale13() = captureCycleInsights(darkTheme = false, fontScale = 1.3f)

    @Test
    fun cycleInsights_populated_dark_scale10() = captureCycleInsights(darkTheme = true, fontScale = 1f)

    @Test
    fun cycleInsights_populated_dark_scale13() = captureCycleInsights(darkTheme = true, fontScale = 1.3f)

    @Test
    fun learn_populated_light_scale10() = captureLearn(darkTheme = false, fontScale = 1f)

    @Test
    fun learn_populated_light_scale13() = captureLearn(darkTheme = false, fontScale = 1.3f)

    @Test
    fun learn_populated_dark_scale10() = captureLearn(darkTheme = true, fontScale = 1f)

    @Test
    fun learn_populated_dark_scale13() = captureLearn(darkTheme = true, fontScale = 1.3f)

    @Test
    fun pregnancyHub_populated_light_scale10() = capturePregnancyHub(darkTheme = false, fontScale = 1f)

    @Test
    fun pregnancyHub_populated_light_scale13() = capturePregnancyHub(darkTheme = false, fontScale = 1.3f)

    @Test
    fun pregnancyHub_populated_dark_scale10() = capturePregnancyHub(darkTheme = true, fontScale = 1f)

    @Test
    fun pregnancyHub_populated_dark_scale13() = capturePregnancyHub(darkTheme = true, fontScale = 1.3f)

    private fun captureToday(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            TodayScreenContent(
                state = M14aGoldenFixtures.todayPopulated(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureCalendar(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            CycleCalendarScreenContent(
                state = M14aGoldenFixtures.calendarPopulated(),
                onDateSelected = {},
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .height(900.dp),
            )
        }
    }

    private fun captureLog(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            LogScreenContent(
                state = M14aGoldenFixtures.logPopulated(),
                actions = M14aGoldenFixtures.logScreenActions(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureHistory(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            HistoryScreenContent(
                periods = M14aGoldenFixtures.historyPopulated(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureOnboarding(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            OnboardingWelcomeScreen(
                onContinue = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureCycleInsights(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            CycleInsightsScreenContent(
                state = M14aGoldenFixtures.cycleInsightsPopulated(),
                onUnlockPremium = {},
                onOpenHistory = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureLearn(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            LearnScreenContent(
                state = M14aGoldenFixtures.learnPopulated(),
                onQueryChange = {},
                onSearch = {},
                onToggleBookmark = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun capturePregnancyHub(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            PregnancyHubScreenContent(
                state = M14aGoldenFixtures.pregnancyHubPopulated(),
                actions = M14aGoldenFixtures.pregnancyHubActions(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
