package dev.mahin.android.cycle.editor

import dev.mahin.core.testing.ViewModelStoreTestHarness
import org.robolectric.shadows.ShadowLooper

/** Clears every Day Editor harness ViewModelStore so DataStore collectors do not leak across tests. */
internal object DayEditorTestVmRegistry {
    private val stores = mutableSetOf<ViewModelStoreTestHarness>()

    fun register(store: ViewModelStoreTestHarness) {
        stores.add(store)
    }

    fun unregister(store: ViewModelStoreTestHarness) {
        stores.remove(store)
    }

    fun clearAll() {
        stores.forEach { it.clear() }
        ShadowLooper.idleMainLooper()
    }
}
