package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.content.ContentArticleDto
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ArticleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

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
    fun loadsArticleAndRecordsRecentView() =
        runTest(testDispatcher) {
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
            viewModel.uiState.filter { it.article != null }.first()
            assertThat(
                viewModel.uiState.value.article
                    ?.title,
            ).isEqualTo("مقاله")
            assertThat(harness.repository.recentArticleIds()).contains("article-1")
            assertThat(analytics.events.any { it.name == "article_opened" }).isTrue()
        }

    @Test
    fun readsCachedArticleWhenOffline() =
        runTest(testDispatcher) {
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
            warm.uiState.filter { it.article != null }.first()
            harness.api.throwOnArticle = true
            val offline = createViewModel("article-2")
            offline.uiState.filter { it.article != null }.first()
            assertThat(
                offline.uiState.value.article
                    ?.title,
            ).isEqualTo("آفلاین")
            assertThat(offline.uiState.value.offline).isFalse()
        }

    private fun createViewModel(articleId: String): ArticleViewModel =
        viewModelStore.hold(
            ArticleViewModel(
                savedStateHandle = SavedStateHandle(mapOf("articleId" to articleId)),
                contentRepository = harness.repository,
                analytics = analytics,
            ),
        )
}
