package dev.mahin.android.learn

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import kotlinx.serialization.json.Json

class LearnContentTestHarness {
    val context: Context = ApplicationProvider.getApplicationContext()
    val database: MahinDatabase =
        Room
            .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
            .allowMainThreadQueries()
            .build()
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
            mediaMemoryCache =
                dev.mahin.core.content
                    .MediaAssetMemoryCache(),
        )

    fun close() {
        database.close()
    }
}
