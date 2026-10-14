package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.learn.ArticleScreenContent
import dev.mahin.android.learn.GuidedPlayerScreenContent
import dev.mahin.android.learn.LearnScreenContent
import dev.mahin.android.learn.ProgramDetailScreenContent
import dev.mahin.core.media.illustration.BundledMahinIllustrationCatalog
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M17bFullScreenGoldenTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test fun learn_magazine_light_scale10() = captureLearn(false, 1f)

    @Test fun learn_magazine_dark_scale10() = captureLearn(true, 1f)

    @Test fun learn_article_light_scale10() = captureArticle(false, 1f)

    @Test fun learn_article_light_scale13() = captureArticle(false, 1.3f)

    @Test fun learn_article_dark_scale10() = captureArticle(true, 1f)

    @Test fun learn_article_dark_scale13() = captureArticle(true, 1.3f)

    @Test fun learn_program_light_scale10() = captureProgram(false, 1f)

    @Test fun learn_program_light_scale13() = captureProgram(false, 1.3f)

    @Test fun learn_program_dark_scale10() = captureProgram(true, 1f)

    @Test fun learn_program_dark_scale13() = captureProgram(true, 1.3f)

    @Test fun learn_player_light_scale10() = capturePlayer(false, 1f)

    @Test fun learn_player_light_scale13() = capturePlayer(false, 1.3f)

    @Test fun learn_player_dark_scale10() = capturePlayer(true, 1f)

    @Test fun learn_player_dark_scale13() = capturePlayer(true, 1.3f)

    private fun captureLearn(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            LearnScreenContent(
                state = M17bGoldenFixtures.learnMagazine(),
                onQueryChange = {},
                onSearch = {},
                onToggleBookmark = {},
                onSelectCategory = {},
                onOpenArticle = {},
                onOpenProgram = {},
                onOpenCollection = {},
                illustrationSource = BundledMahinIllustrationCatalog,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureArticle(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            ArticleScreenContent(state = M17bGoldenFixtures.article(), modifier = Modifier.fillMaxSize())
        }
    }

    private fun captureProgram(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            ProgramDetailScreenContent(
                state = M17bGoldenFixtures.programDetail(),
                onStartSession = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun capturePlayer(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            GuidedPlayerScreenContent(
                state = M17bGoldenFixtures.player(),
                onPause = {},
                onResume = {},
                onComplete = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
