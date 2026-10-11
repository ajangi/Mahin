package dev.mahin.android.cycle

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarLegendPersistenceTest {
    private lateinit var context: Context
    private lateinit var repository: CalendarUiPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = CalendarUiPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        runBlocking { repository.clear() }
    }

    @Test
    fun userReExpand_afterAutoCollapse_staysExpandedOnNextDayTap() {
        runBlocking {
            repository.markLegendAutoCollapsedOnce()
            repository.setLegendCollapsed(false)
            assertThat(repository.observeLegendAutoCollapsedOnce().first()).isTrue()
            assertThat(repository.observeLegendCollapsed().first()).isFalse()
            repository.setLegendCollapsed(true)
            repository.setLegendCollapsed(false)
            assertThat(repository.observeLegendCollapsed().first()).isFalse()
        }
    }
}
