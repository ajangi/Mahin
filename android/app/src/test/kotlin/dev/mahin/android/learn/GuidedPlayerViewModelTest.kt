package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
class GuidedPlayerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var harness: LearnContentTestHarness
    private lateinit var countingPersistence: CountingGuidedPlayerStatePersistence
    private val viewModelStore = ViewModelStoreTestHarness()
    private val analytics = RecordingAnalyticsGateway()
    private val clock = MutableInstantClock()
    private lateinit var ticker: ManualPlayerSessionTicker

    @Before
    fun setUp() {
        harness = LearnContentTestHarness()
        countingPersistence = CountingGuidedPlayerStatePersistence(harness.localStore)
        clock.epochMillis = 0L
        ticker = ManualPlayerSessionTicker()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        harness.close()
    }

    @Test
    fun offlineProgram_showsErrorWithoutStartingTimer() =
        runTest(testDispatcher) {
            harness.api.throwOnProgram = true
            val viewModel = createViewModel("missing", "sess-1")
            val state = viewModel.uiState.filter { it.loadError }.first()
            assertThat(state.offline).isTrue()
            assertThat(state.stepCount).isEqualTo(0)
        }

    @Test
    fun emptySessionSteps_showsErrorWithoutStartingTimer() =
        runTest(testDispatcher) {
            harness.api.programs["prog-empty"] =
                sampleProgram(programId = "prog-empty", sessionId = "sess-empty").copy(
                    sessions =
                        listOf(
                            sampleProgram().sessions.first().copy(steps = emptyList()),
                        ),
                )
            val viewModel = createViewModel("prog-empty", "sess-empty")
            val state = viewModel.uiState.filter { it.loadError }.first()
            assertThat(state.offline).isTrue()
        }

    @Test
    fun manyTicksWithinOneStep_persistsAtMostOnce() =
        runTest(testDispatcher) {
            harness.api.programs["prog-1"] = sampleProgram()
            val viewModel = createViewModel("prog-1", "sess-1")
            awaitSessionLoaded(viewModel)
            countingPersistence.saveCount = 0
            repeat(10) {
                clock.epochMillis += 250L
                ticker.advanceOneTick()
                advanceUntilIdle()
            }
            assertThat(countingPersistence.saveCount).isAtMost(1)
        }

    @Test
    fun processDeath_saveThenRestoreRoundTrip() =
        runTest(testDispatcher) {
            harness.api.programs["prog-1"] = sampleProgram()
            val first = createViewModel("prog-1", "sess-1")
            awaitSessionLoaded(first)
            ticker.advanceOneTick()
            advanceUntilIdle()
            clock.epochMillis += 1_500L
            ticker.advanceOneTick()
            advanceUntilIdle()
            first.pause()
            awaitPersisted { harness.localStore.loadPlayerState() != null }
            val saved = harness.localStore.loadPlayerState()
            assertThat(saved).isNotNull()
            assertThat(saved!!.paused).isTrue()
            assertThat(saved.stepElapsedMs).isEqualTo(1_500L)
            assertThat(saved.stepIndex).isEqualTo(0)

            viewModelStore.clear()
            advanceUntilIdle()

            val restored = createViewModel("prog-1", "sess-1")
            val state =
                restored.uiState
                    .filter {
                        it.stepInstruction.isNotEmpty() && it.paused && it.snapshot.stepElapsedMs == 1_500L
                    }.first()
            assertThat(state.stepIndex).isEqualTo(0)
            assertThat(state.stepInstruction).isEqualTo("قدم اول")
        }

    @Test
    fun cachedProgram_playsWhenApiThrows() =
        runTest(testDispatcher) {
            harness.api.programs["prog-1"] = sampleProgram()
            val warm = createViewModel("prog-1", "sess-1")
            awaitSessionLoaded(warm)
            viewModelStore.clear()
            advanceUntilIdle()

            harness.api.throwOnProgram = true
            val offlinePlayer = createViewModel("prog-1", "sess-1")
            val state =
                offlinePlayer.uiState
                    .filter {
                        it.stepInstruction.isNotEmpty() && !it.loadError
                    }.first()
            assertThat(state.offline).isFalse()
            assertThat(state.sessionTitle).isEqualTo("جلسه")
        }

    @Test
    fun completeSession_logsCompletionAndClearsPersistedState() =
        runTest(testDispatcher) {
            harness.api.programs["prog-1"] = sampleProgram()
            val viewModel = createViewModel("prog-1", "sess-1")
            awaitSessionLoaded(viewModel)
            viewModel.pause()
            awaitPersisted { harness.localStore.loadPlayerState() != null }

            viewModel.completeSession()
            awaitPersisted { harness.localStore.loadPlayerState() == null }
            assertThat(harness.localStore.programCompletionCount("prog-1")).isEqualTo(1)
        }

    private suspend fun awaitSessionLoaded(viewModel: GuidedPlayerViewModel) {
        viewModel.uiState.filter { it.stepInstruction.isNotEmpty() && !it.loadError }.first()
    }

    private suspend fun TestScope.awaitPersisted(predicate: suspend () -> Boolean) {
        repeat(50) {
            if (predicate()) return
            advanceUntilIdle()
        }
        throw AssertionError("Persistence condition not met")
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
                playerStatePersistence = countingPersistence,
                clock = clock,
                analytics = analytics,
                ticker = ticker,
            ),
        )
}
