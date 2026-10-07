package dev.mahin.core.media.illustration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.MediaApprovalStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MahinIllustrationGatingTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val fixtures = TestMahinIllustrationFixtures.gatedCatalog

    @Test
    fun medicalFamily_withoutApproved_rendersNeutralFrameWithoutImagery() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(
                    assetId = TestMahinIllustrationFixtures.PREGNANCY_UNAPPROVED,
                    source = fixtures,
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun cycleEducation_throughComposable_isGatedWithoutImagery() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(
                    assetId = TestMahinIllustrationFixtures.CYCLE_EDUCATION_DRAFT,
                    source = fixtures,
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun medicalPlaceholder_neverRendersImagery_evenIfMislabelledApproved() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(
                    assetId = TestMahinIllustrationFixtures.PREGNANCY_MISLABELLED_PLACEHOLDER,
                    source = fixtures,
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun unknownMedicalFamilyId_failsSafeWithoutImagery() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(
                    assetId = TestMahinIllustrationFixtures.PREGNANCY_UNKNOWN,
                    source = fixtures,
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    @Test
    fun approvedNonPlaceholderMedicalAsset_rendersImagery() {
        composeRule.setContent {
            MahinTheme(darkTheme = true) {
                MahinIllustration(
                    assetId = TestMahinIllustrationFixtures.PREGNANCY_APPROVED,
                    source = fixtures,
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertIsDisplayed()
    }

    @Test
    fun editorialPlaceholder_rendersImageryThroughProductionCatalog() {
        composeRule.setContent {
            MahinTheme {
                MahinIllustration(assetId = "editorial/empty-state/v1")
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertIsDisplayed()
    }

    @Test
    fun publishedStatus_countsAsApprovedForGating() {
        val meta = fixtures.metadataFor(TestMahinIllustrationFixtures.PREGNANCY_APPROVED)
        assertThat(meta).isNotNull()
        assertThat(meta!!.approvalStatus).isEqualTo(MediaApprovalStatus.PUBLISHED)
        assertThat(meta.mayRenderBundledImagery()).isTrue()
    }
}
