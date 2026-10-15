package dev.mahin.android.pregnancy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.media.R
import dev.mahin.core.media.illustration.MahinIllustrationMetadata
import dev.mahin.core.media.illustration.MahinIllustrationSource
import dev.mahin.core.media.illustration.MahinIllustrationTestTags
import dev.mahin.core.model.MediaApprovalStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR")
class PregnancyWeekByWeekIllustrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun approvedAsset_showsImagery() {
        composeRule.setContent {
            MahinTheme {
                PregnancyWeekByWeekScreenContent(
                    state = weekState("pregnancy/week-12/v1"),
                    illustrationSource = fixtureSource(approved = true),
                    onWeekSelected = {},
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertIsDisplayed()
    }

    @Test
    fun draftMedicalGatedAsset_showsNeutralFrameOnly() {
        composeRule.setContent {
            MahinTheme {
                PregnancyWeekByWeekScreenContent(
                    state = weekState("pregnancy/week-12/v1"),
                    illustrationSource = fixtureSource(approved = false),
                    onWeekSelected = {},
                )
            }
        }
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    private fun weekState(assetId: String): PregnancyWeekByWeekUiState =
        PregnancyWeekByWeekUiState(
            isLoading = false,
            currentWeek = 12,
            pages =
                listOf(
                    PregnancyWeekPageState(
                        weekNumber = 12,
                        title = "هفته ۱۲",
                        fetalSummary = "نمونه",
                        maternalSummary = "نمونه",
                        sizeComparisonLabel = null,
                        illustrationAssetId = assetId,
                    ),
                ),
        )

    private fun fixtureSource(approved: Boolean): MahinIllustrationSource =
        object : MahinIllustrationSource {
            override fun metadataFor(assetId: String): MahinIllustrationMetadata =
                MahinIllustrationMetadata(
                    assetId = assetId,
                    drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                    placeholder = false,
                    approvalStatus = if (approved) MediaApprovalStatus.PUBLISHED else MediaApprovalStatus.DRAFT,
                    contentDescriptionRes = null,
                    altText = "نمونه",
                    medicalGoverned = true,
                )
        }
}
