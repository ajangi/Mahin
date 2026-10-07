package dev.mahin.android.navigation

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.ReproductiveMode
import org.junit.Test

class MahinTopLevelDestinationTest {
    @Test
    fun forMode_cycleTracking_returnsFiveTabs() {
        val tabs = MahinTopLevelDestination.forMode(ReproductiveMode.CYCLE_TRACKING)
        assertThat(tabs).hasSize(5)
        assertThat(tabs.map { it.route }).containsExactly(
            "today",
            "calendar",
            "log",
            "cycle_insights",
            "learn",
        )
    }

    @Test
    fun forMode_ttc_returnsFiveTabs() {
        val tabs = MahinTopLevelDestination.forMode(ReproductiveMode.TRYING_TO_CONCEIVE)
        assertThat(tabs).hasSize(5)
        assertThat(tabs.map { it.route }).containsExactly(
            "today",
            "calendar",
            "log",
            "ttc_insights",
            "learn",
        )
    }

    @Test
    fun forMode_pregnant_returnsFiveTabs() {
        val tabs = MahinTopLevelDestination.forMode(ReproductiveMode.PREGNANT)
        assertThat(tabs).hasSize(5)
        assertThat(tabs.map { it.route }).containsExactly(
            "today",
            "pregnancy_hub",
            "log",
            "plan",
            "learn",
        )
    }

    @Test
    fun forMode_postPregnancy_returnsCycleInsightsTabs() {
        val tabs = MahinTopLevelDestination.forMode(ReproductiveMode.POST_PREGNANCY_TRANSITION)
        assertThat(tabs.map { it.route }).containsExactly(
            "today",
            "calendar",
            "log",
            "cycle_insights",
            "learn",
        )
    }

    @Test
    fun forMode_trackingPaused_returnsCycleInsightsTabs() {
        val tabs = MahinTopLevelDestination.forMode(ReproductiveMode.TRACKING_PAUSED)
        assertThat(tabs.map { it.route }).containsExactly(
            "today",
            "calendar",
            "log",
            "cycle_insights",
            "learn",
        )
    }
}
