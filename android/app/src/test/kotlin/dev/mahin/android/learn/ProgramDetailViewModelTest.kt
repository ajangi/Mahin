package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
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
class ProgramDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var harness: LearnContentTestHarness
    private val viewModelStore = ViewModelStoreTestHarness()

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
    fun loadsProgramAndCompletionCount() =
        runTest(testDispatcher) {
            harness.api.programs["prog-1"] = sampleProgram()
            harness.localStore.logSessionCompletion("c1", "prog-1", "sess-1")
            val viewModel =
                viewModelStore.hold(
                    ProgramDetailViewModel(
                        savedStateHandle = SavedStateHandle(mapOf("programId" to "prog-1")),
                        contentRepository = harness.repository,
                        localStore = harness.localStore,
                    ),
                )
            viewModel.uiState.filter { it.program != null }.first()
            assertThat(
                viewModel.uiState.value.program
                    ?.title,
            ).isEqualTo("برنامه")
            assertThat(viewModel.uiState.value.completionCount).isEqualTo(1)
        }
}
