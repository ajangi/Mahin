package dev.mahin.android.learn

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.MediaAssetMemoryCache
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.core.testing.buildMahinInMemoryDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class LearnContentTestHarness(
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val viewModelStore: ViewModelStoreTestHarness? = null,
) {
    val context: Context = ApplicationProvider.getApplicationContext()
    val database: MahinDatabase = buildMahinInMemoryDatabase(context)
    val api = FakeContentApi()
    val json =
        Json {
            ignoreUnknownKeys = true
        }
    val cachePreferences = ContentCachePreferencesRepository(context)
    val localStore = WellnessContentLocalStore(database.wellnessContentDao(), json)
    val repository =
        ContentRepository(
            api = api,
            cachePreferences = cachePreferences,
            localStore = localStore,
            json = json,
            mediaMemoryCache = MediaAssetMemoryCache(),
            ioDispatcher = ioDispatcher,
        )

    fun close() {
        viewModelStore?.clear()
        database.close()
    }
}
