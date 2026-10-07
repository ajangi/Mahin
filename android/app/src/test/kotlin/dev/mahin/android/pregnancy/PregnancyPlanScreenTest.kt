package dev.mahin.android.pregnancy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.android.golden.M14aGoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class PregnancyPlanScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun planTab_rendersAppointmentsSection() {
        composeRule.setContent {
            MahinTheme {
                PregnancyPlanScreenContent(
                    state = M14aGoldenFixtures.pregnancyHubPopulated(),
                    actions = M14aGoldenFixtures.pregnancyHubActions(),
                )
            }
        }
        composeRule.onNodeWithTag("pregnancy_plan_list").assertIsDisplayed()
        composeRule.onNodeWithTag("pregnancy_plan_list").performScrollToNode(hasText("قرارها و آزمایش‌ها"))
    }
}
