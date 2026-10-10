package dev.mahin.android.cycle

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class TodayRingA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun todayCycleHero_ringContentDescription_isSingleExactSentence() {
        val state = M15GoldenFixtures.todayRingTalkBack()
        val expected = "روز ۱۴ چرخه، پریود بعدی حدود ۵ روز دیگر، تخمینی"
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(state = state)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen_list").performScrollToNode(hasTestTag("today_cycle_hero"))
        val matcher =
            SemanticsMatcher("ring content description") { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    ?.joinToString() == expected
            }
        composeRule.onNode(matcher).assertIsDisplayed()
        val description =
            composeRule
                .onNode(matcher)
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription]
                .joinToString()
        assertThat(description).isEqualTo(expected)
    }
}
