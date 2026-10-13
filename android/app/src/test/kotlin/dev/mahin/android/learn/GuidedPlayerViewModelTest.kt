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
class GuidedPlayerViewModelTest {
    private val harness = LearnContentTestHarness()
    private val viewModelStore = ViewModelStoreTestHarness()
    private val analytics = RecordingAnalyticsGateway()
    private val clock = MutableInstantClock()
    private val ticker = ManualPlayerSessionTicker()

    @After
    fun tearDown() {
        viewModelStore.clear()
        harness.close()
    }

    @Test
    fun offlineProgram_showsErrorWithoutStartingTimer() {
        runBlocking {
            harness.api.throwOnProgram = true
            val viewModel = createViewModel("missing", "sess-1")
            awaitUntil { viewModel.uiState.value.loadError }
            assertThat(viewModel.uiState.value.offline).isTrue()
            assertThat(viewModel.uiState.value.stepCount).isEqualTo(0)
        }
    }

    @Test
    fun emptySessionSteps_showsErrorWithoutStartingTimer() {
        runBlocking {
            harness.api.programs["prog-empty"] =
                sampleProgram(programId = "prog-empty", sessionId = "sess-empty").copy(
                    sessions =
                        listOf(
                            sampleProgram().sessions.first().copy(steps = emptyList()),
                        ),
                )
            val viewModel = createViewModel("prog-empty", "sess-empty")
            awaitUntil { viewModel.uiState.value.loadError }
            assertThat(viewModel.uiState.value.offline).isTrue()
        }
    }

    @Test
    fun processDeath_restoresStepElapsedAndPausedState() {
        runBlocking {
            harness.api.programs["prog-1"] = sampleProgram()
            val first =
                createViewModel(
                    programId = "prog-1",
                    sessionId = "sess-1",
                )
            awaitUntil { first.uiState.value.stepInstruction == "قدم اول" }
            clock.epochMillis += 1_000
            ticker.advanceOneTick()
            ShadowLooper.idleMainLooper()
            first.pause()
            awaitUntil { first.uiState.value.paused }
            val saved = harness.localStore.loadPlayerState()
            assertThat(saved?.stepIndex).isEqualTo(0)
            assertThat(saved?.stepElapsedMs).isAtLeast(1_000L)
            assertThat(saved?.paused).isTrue()

            val restored = createViewModel("prog-1", "sess-1")
            awaitUntil { restored.uiState.value.paused }
            assertThat(restored.uiState.value.stepIndex).isEqualTo(0)
            assertThat(restored.uiState.value.snapshot.stepElapsedMs).isAtLeast(1_000L)
        }
    }

    @Test
    fun completeSession_logsCompletionAndClearsPersistedState() {
        runBlocking {
            harness.api.programs["prog-1"] = sampleProgram()
            val viewModel = createViewModel("prog-1", "sess-1")
            awaitUntil {
                viewModel.uiState.value.stepInstruction
                    .isNotEmpty()
            }
            viewModel.completeSession()
            awaitUntil { harness.localStore.programCompletionCount("prog-1") == 1 }
            assertThat(harness.localStore.loadPlayerState()).isNull()
        }
    }

    private fun createViewModel(
        programId: String,
        sessionId: String,
    ): GuidedPlayerViewModel =
        viewModelStore.hold(
            GuidedPlayerViewModel(
                savedStateHandle =
                    SavedStateHandle(
                        mapOf(
                            "programId" to programId,
                            "sessionId" to sessionId,
                        ),
                    ),
                contentRepository = harness.repository,
                localStore = harness.localStore,
                clock = clock,
                analytics = analytics,
                ticker = ticker,
            ),
        )

    private suspend fun awaitUntil(predicate: suspend () -> Boolean) {
        repeat(400) {
            if (predicate()) return
            delay(10)
            ShadowLooper.idleMainLooper()
            clock.epochMillis += 250
        }
        throw AssertionError("Condition not met")
    }
}
