package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class PregnancyStartSheetScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pregnancyStartSheet_largeFontScale_showsConfirmButton() {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density = 1f, fontScale = 2f),
            ) {
                MahinTheme {
                    Box(modifier = Modifier.height(420.dp)) {
                        PregnancyStartSheetContent(
                            onConfirm = { _, _ -> },
                            modifier =
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .testTag("pregnancy_start_sheet_scroll"),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag("pregnancy_start_sheet_scroll")
            .performScrollToNode(hasTestTag("pregnancy_start_confirm"))
        composeRule.onNodeWithTag("pregnancy_start_confirm").assertIsDisplayed()
    }
}
