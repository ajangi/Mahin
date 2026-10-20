package dev.mahin.core.testing

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

/** Holds ViewModels in a [ViewModelStore] so tests can [clear] before tearing down DataStore / DB. */
class ViewModelStoreTestHarness {
    private val store = ViewModelStore()

    fun <T : ViewModel> hold(
        modelClass: Class<T>,
        factory: ViewModelProvider.Factory,
    ): T = ViewModelProvider(store, factory)[modelClass]

    @SuppressLint("RestrictedApi")
    fun <T : ViewModel> hold(viewModel: T): T {
        val key = viewModel::class.java.canonicalName ?: viewModel::class.java.name
        store.put(key, viewModel)
        return viewModel
    }

    fun clear() {
        store.clear()
    }
}
