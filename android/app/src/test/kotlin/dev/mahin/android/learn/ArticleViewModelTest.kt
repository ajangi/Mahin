package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.content.ContentArticleDto
import dev.mahin.core.testing.ViewModelStoreTestHarness
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ArticleViewModelTest {
    private lateinit var harness: LearnContentTestHarness
    private val viewModelStore = ViewModelStoreTestHarness()
    private val analytics = RecordingAnalyticsGateway()

    @Before
    fun setUp() {
        harness = LearnContentTestHarness()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        harness.close()
    }

    @Test
    fun loadsArticleAndRecordsRecentView() {
        runBlocking {
            harness.api.articles["article-1"] =
                ContentArticleDto(
                    id = "article-1",
                    slug = "a",
                    locale = "fa-IR",
                    title = "مقاله",
                    contentType = "article",
                    status = "published",
                    medicalRiskLevel = "none",
                    contentVersion = 1,
                )
            val viewModel = createViewModel("article-1")
            awaitUntil { !viewModel.uiState.value.loading }
            assertThat(
                viewModel.uiState.value.article
                    ?.title,
            ).isEqualTo("مقاله")
            assertThat(harness.repository.recentArticleIds()).contains("article-1")
            assertThat(analytics.events.any { it.name == "article_opened" }).isTrue()
        }
    }

    @Test
    fun readsCachedArticleWhenOffline() {
        runBlocking {
            val article =
                ContentArticleDto(
                    id = "article-2",
                    slug = "b",
                    locale = "fa-IR",
                    title = "آفلاین",
                    contentType = "article",
                    status = "published",
                    medicalRiskLevel = "none",
                    contentVersion = 1,
                )
            harness.api.articles["article-2"] = article
            val warm = createViewModel("article-2")
            awaitUntil { !warm.uiState.value.loading }
            harness.api.throwOnArticle = true
            val offline = createViewModel("article-2")
            awaitUntil { !offline.uiState.value.loading }
            assertThat(
                offline.uiState.value.article
                    ?.title,
            ).isEqualTo("آفلاین")
            assertThat(offline.uiState.value.offline).isFalse()
        }
    }

    private fun createViewModel(articleId: String): ArticleViewModel =
        viewModelStore.hold(
            ArticleViewModel(
                savedStateHandle = SavedStateHandle(mapOf("articleId" to articleId)),
                contentRepository = harness.repository,
                analytics = analytics,
            ),
        )

    private suspend fun awaitUntil(predicate: () -> Boolean) {
        repeat(200) {
            if (predicate()) return
            delay(10)
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met")
    }
}
