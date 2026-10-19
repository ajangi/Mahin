package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertWithMessage
import dev.mahin.core.testing.ViewModelStoreTestHarness

/** Tracks Day Editor harness [ViewModelStoreTestHarness] instances; fails if any are left open. */
internal object DayEditorTestVmRegistry {
    private val openStores = linkedMapOf<String, ViewModelStoreTestHarness>()

    fun register(
        owner: String,
        store: ViewModelStoreTestHarness,
    ) {
        openStores[owner] = store
    }

    fun unregister(store: ViewModelStoreTestHarness) {
        openStores.entries.removeIf { it.value === store }
    }

    fun assertAllStoresClosed() {
        assertWithMessage(
            "DayEditor ViewModelStore not closed before test teardown: ${openStores.keys.joinToString()}",
        ).that(openStores).isEmpty()
    }
}
