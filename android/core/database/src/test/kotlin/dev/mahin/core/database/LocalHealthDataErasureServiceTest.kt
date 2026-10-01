package dev.mahin.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocalHealthDataErasureServiceTest {
    private lateinit var context: Context
    private lateinit var database: MahinDatabase
    private lateinit var tombstones: HealthConnectPeriodDayTombstoneRepository
    private lateinit var healthConnectPreferences: HealthConnectPreferencesRepository
    private lateinit var erasureService: LocalHealthDataErasureService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        tombstones = HealthConnectPeriodDayTombstoneRepository(context)
        healthConnectPreferences = HealthConnectPreferencesRepository(context)
        val databaseProvider =
            object : MahinDatabaseProvider {
                override fun database(): MahinDatabase = database
            }
        erasureService =
            LocalHealthDataErasureService(
                databaseProvider = databaseProvider,
                healthConnectPeriodDayTombstoneRepository = tombstones,
                healthConnectPreferencesRepository = healthConnectPreferences,
            )
    }

    @Test
    fun eraseAll_clearsHealthConnectTombstonesAndPreferences() =
        runBlocking {
            val date = LocalDate.of(2025, 4, 1)
            tombstones.markUserDeleted(date)
            healthConnectPreferences.setUserOptIn(true)
            healthConnectPreferences.setPermissionsPreviouslyGranted(true)

            erasureService.eraseAllLocalHealthData()

            assertThat(tombstones.isUserDeleted(date)).isFalse()
            assertThat(healthConnectPreferences.snapshot.first().userOptIn).isFalse()
            assertThat(healthConnectPreferences.snapshot.first().permissionsPreviouslyGranted).isFalse()
        }
}
