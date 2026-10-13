package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.testing.ViewModelStoreTestHarness
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProgramDetailViewModelTest {
    private val harness = LearnContentTestHarness()
    private val viewModelStore = ViewModelStoreTestHarness()

    @After
    fun tearDown() {
        viewModelStore.clear()
        harness.close()
    }

    @Test
    fun loadsProgramAndCompletionCount() {
        runBlocking {
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
            awaitUntil { !viewModel.uiState.value.loading }
            assertThat(
                viewModel.uiState.value.program
                    ?.title,
            ).isEqualTo("برنامه")
            assertThat(viewModel.uiState.value.completionCount).isEqualTo(1)
        }
    }

    private suspend fun awaitUntil(predicate: () -> Boolean) {
        repeat(200) {
            if (predicate()) return
            delay(10)
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met")
    }
}
