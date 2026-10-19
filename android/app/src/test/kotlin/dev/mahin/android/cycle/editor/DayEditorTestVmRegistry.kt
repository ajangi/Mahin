package dev.mahin.android.cycle.editor

import dev.mahin.core.testing.ViewModelStoreTestHarness
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.robolectric.shadows.ShadowLooper

/** Clears every Day Editor harness ViewModelStore so DataStore collectors do not leak across tests. */
internal object DayEditorTestVmRegistry {
    private val stores = mutableSetOf<ViewModelStoreTestHarness>()
    var scheduler: TestCoroutineScheduler? = null

    fun register(store: ViewModelStoreTestHarness) {
        stores.add(store)
    }

    fun unregister(store: ViewModelStoreTestHarness) {
        stores.remove(store)
    }

    fun clearAll() {
        stores.forEach { it.clear() }
        scheduler?.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        scheduler?.advanceUntilIdle()
    }
}
