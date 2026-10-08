package dev.mahin.android.cycle

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.android.R
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinCycleProgressRing
import dev.mahin.domain.cycle.TodaySnapshot
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun cycleRing_hasSingleContentDescription_withoutSeparateDayNode() {
        val hero = (M15GoldenFixtures.todayFertileWindow().todaySnapshot as TodaySnapshot.Cycle).hero
        val statusLine = "۵ روز تا پریود بعدی (تخمینی)"
        composeRule.setContent {
            MahinTheme {
                val cycleDay = hero.cycleDay!!
                val a11y =
                    stringResource(
                        R.string.today_ring_content_description,
                        PersianDigits.format(cycleDay),
                        statusLine,
                    )
                MahinCycleProgressRing(
                    arcs = TodayCycleHeroMapper.ringArcs(hero),
                    progressFraction = TodayCycleHeroMapper.progressFraction(hero),
                    trackColor = TodayCycleHeroMapper.trackColor(),
                    progressColor = MaterialTheme.colorScheme.primary,
                    todayMarkerFraction = TodayCycleHeroMapper.todayMarkerFraction(hero),
                    todayMarkerColor = MaterialTheme.colorScheme.primary,
                    contentDescription = a11y,
                    modifier = Modifier.testTag("today_ring_a11y_under_test"),
                ) {
                    Text(
                        text = stringResource(R.string.today_cycle_day_numeric, PersianDigits.format(cycleDay)),
                        modifier = Modifier.semantics { invisibleToUser() },
                    )
                }
            }
        }
        val ringNode = composeRule.onNodeWithTag("today_ring_a11y_under_test").fetchSemanticsNode()
        val description = ringNode.config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue(description.contains("چرخه"))
        val hasVisibleDayNumber =
            ringNode.children.any { child ->
                child.config[SemanticsProperties.Text].any { text -> text.contains("۱۴") }
            }
        assertFalse(hasVisibleDayNumber)
    }
}
