package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.android.golden.M16GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.testing.ViewModelStoreClearingRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class DayEditorManageCategoriesPanelTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun managePanel_showsPersianCategoryReorderLabels() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorManageCategoriesPanel(
                    state = M16GoldenFixtures.manageCategories(),
                    onLifestyleToggle = { _, _ -> },
                    onRequestSexualOptIn = {},
                )
            }
        }
        androidComposeRule
            .onNodeWithTag("day_editor_category_order_PAIN")
            .assertTextContains("درد", substring = true)
    }
}
