package dev.mahin.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MahinDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MahinDatabase::class.java,
        )

    @Test
    fun migrate1To2_addsCycleTables() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO app_meta (key, value) VALUES ('schema', 'v1')")
            close()
        }
        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        migrated.query("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").use { cursor ->
            val tables = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0))
            }
            assertThat(tables).contains("cycle_profile")
            assertThat(tables).contains("period_record")
            assertThat(tables).contains("daily_log")
        }
        migrated.close()
    }

    @Test
    fun migrate2To3_addsTtcDayLog() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                INSERT INTO cycle_profile (
                    id, reproductiveMode, typicalCycleLengthDays, typicalPeriodLengthDays,
                    regularity, onboardingCompleted, updatedAtEpochMs
                ) VALUES (1, 'CYCLE_TRACKING', 28, 5, 'UNKNOWN', 1, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)
        migrated.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ttc_day_log'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
        }
        migrated.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
