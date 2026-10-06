package dev.mahin.core.media.illustration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MahinIllustrationGatingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun medicalFamily_withoutApproved_rendersNeutralFrameWithoutImagery() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(assetId = "pregnancy/fetal-development/week-18/v3")
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun medicalPlaceholder_neverRendersImagery_evenIfMislabelledApproved() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(assetId = "pregnancy/placeholder/mislabelled/v1")
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun unknownAssetId_failsSafeWithoutImagery() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(assetId = "unknown/asset/id/v1")
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun editorialPlaceholder_rendersImageryThroughMahinIllustration() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(assetId = "editorial/empty-state/v1")
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertIsDisplayed()
    }

    @Test
    fun mayRenderBundledImagery_usesRealCatalogMetadata() {
        val meta = BundledMahinIllustrationCatalog.metadataFor("cycle/education/ovulation/v2")
        assertThat(meta).isNotNull()
        assertThat(meta!!.mayRenderBundledImagery()).isFalse()
    }
}
