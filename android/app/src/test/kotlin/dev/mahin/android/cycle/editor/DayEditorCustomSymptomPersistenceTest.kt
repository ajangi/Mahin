package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
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
class DayEditorCustomSymptomPersistenceTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 5, 5)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking { viewModel.openDate(day) }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun addCustomSymptom_persistsLocalTagOnly() {
        runBlocking {
            viewModel.addCustomSymptom("سردرد شخصی")
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log?.localCustomSymptomTags?.contains("سردرد شخصی") == true
            }
            val log = harness.dailyLogFor(day)
            assertThat(log?.symptomTags).doesNotContain("سردرد شخصی")
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
