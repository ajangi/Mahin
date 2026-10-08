package dev.mahin.android.cycle

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class TodayRingA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cycleRing_hasSingleContentDescription_withoutSeparateDayNode() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(state = M15GoldenFixtures.todayFertileWindow())
            }
        }
        val ringNode = composeRule.onNodeWithTag("cycle_progress_ring").fetchSemanticsNode()
        val description = ringNode.config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(description.contains("چرخه"))
        val hasVisibleDayNumber =
            ringNode.children.any { child ->
                child.config[SemanticsProperties.Text].any { text -> text.contains("۱۴") }
            }
        assertFalse(hasVisibleDayNumber)
    }
}
