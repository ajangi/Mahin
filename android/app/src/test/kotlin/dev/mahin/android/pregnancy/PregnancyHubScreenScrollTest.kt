package dev.mahin.android.pregnancy

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.domain.pregnancy.GestationalAge
import dev.mahin.domain.pregnancy.PregnancyDatingSnapshot
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import dev.mahin.domain.pregnancy.PregnancyTrimester
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class PregnancyHubScreenScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pregnancyHubScreen_largeFontScale_showsSafetyDisclaimer() {
        val dating =
            PregnancyDatingSnapshot(
                lmpDate = LocalDate.of(2025, 1, 1),
                lmpBasedEdd = LocalDate.of(2025, 10, 8),
                clinicalEddDate = null,
                effectiveEddDate = LocalDate.of(2025, 10, 8),
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
            )
        val status =
            PregnancyStatusSnapshot(
                dating = dating,
                gestationalAge = GestationalAge(weeks = 12, days = 3, totalDays = 87),
                trimester = PregnancyTrimester.SECOND,
                daysUntilEdd = 193,
                displayWeekNumber = 13,
            )
        val disclaimer =
            "اطلاعات این بخش جایگزین مراقبت پزشکی نیست. در صورت نگرانی با متخصص مشورت کنید."
        composeRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density = 1f, fontScale = 2f),
            ) {
                MahinTheme {
                    PregnancyHubScreenContent(
                        state =
                            PregnancyHubContentState(
                                isLoading = false,
                                isPregnantMode = true,
                                postTransition = false,
                                status = status,
                                kickSessionActive = false,
                                kickCount = 0,
                                kickElapsedSeconds = 0L,
                                contractionSessionActive = false,
                                contractionInProgress = false,
                                contractionElapsedSeconds = 0L,
                                selectedOutcome = null,
                                wantsSupportContent = false,
                                suppressCelebratoryNotifications = false,
                            ),
                        actions =
                            PregnancyHubActions(
                                onStartKickSession = {},
                                onStopKickSession = {},
                                onRecordKick = {},
                                onStartContractionSession = {},
                                onEndContractionSession = {},
                                onToggleContraction = {},
                                onOutcomeSelected = {},
                                onSupportContentToggle = {},
                                onSaveOutcome = {},
                                onResumeCycle = {},
                                onResumeTtc = {},
                            ),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pregnancy_hub_list").performScrollToIndex(7)
        composeRule.onNodeWithText(disclaimer, substring = true).assertIsDisplayed()
    }
}
