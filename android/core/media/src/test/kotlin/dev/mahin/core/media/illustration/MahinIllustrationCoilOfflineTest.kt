package dev.mahin.core.media.illustration

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.MediaApprovalStatus
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MahinIllustrationCoilOfflineTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun remoteLoadFailure_rendersNeutralFrameWithDescriptionAndNoImagery() {
        val offlineLoader =
            ImageLoader
                .Builder(composeRule.activity)
                .components {
                    add(OfflineFetcherFactory())
                }.build()
        val source =
            object : MahinIllustrationSource {
                override fun metadataFor(assetId: String): MahinIllustrationMetadata? =
                    MahinIllustrationMetadata(
                        assetId = assetId,
                        drawableRes = null,
                        remoteUrl = "https://offline.example.test/asset.png",
                        placeholder = false,
                        approvalStatus = MediaApprovalStatus.PUBLISHED,
                        contentDescriptionRes = null,
                        altText = "توضیح آفلاین",
                        medicalGoverned = false,
                    )
            }
        composeRule.setContent {
            MahinTheme {
                CompositionLocalProvider(LocalImageLoader provides offlineLoader) {
                    MahinIllustration(
                        assetId = "remote-offline",
                        source = source,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.FRAME).assertIsDisplayed()
        composeRule
            .onNode(hasContentDescription("توضیح آفلاین"), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag(MahinIllustrationTestTags.IMAGERY).assertDoesNotExist()
    }

    private class OfflineFetcherFactory : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? =
            if (data.startsWith("http")) {
                object : Fetcher {
                    override suspend fun fetch(): FetchResult = throw IOException("offline")
                }
            } else {
                null
            }
    }
}
