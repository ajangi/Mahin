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
            execSQL(
                """
                INSERT INTO daily_log (
                    id, logDate, moodTags, symptomTags, painSeverity, note, updatedAtEpochMs
                ) VALUES ('d1', '2025-01-01', '', 'سردرد', NULL, NULL, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)
        migrated.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ttc_day_log'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
        }
        migrated.query("SELECT reproductiveMode FROM cycle_profile WHERE id = 1").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("CYCLE_TRACKING")
        }
        migrated.query("SELECT symptomTags FROM daily_log WHERE id = 'd1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("سردرد")
        }
        migrated.close()
    }

    @Test
    fun migrate3To4_addsPregnancyTablesAndPreservesPriorRows() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO cycle_profile (
                    id, reproductiveMode, typicalCycleLengthDays, typicalPeriodLengthDays,
                    regularity, onboardingCompleted, updatedAtEpochMs
                ) VALUES (1, 'TRYING_TO_CONCEIVE', 28, 5, 'UNKNOWN', 1, 0)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO ttc_day_log (
                    id, logDate, bbtCelsius, ovulationTestResult, cervicalMucus,
                    intercourseLogged, intercourseProtected, pregnancyTestResult, updatedAtEpochMs
                ) VALUES ('t1', '2025-02-01', 36.5, NULL, NULL, 0, NULL, 'POSITIVE', 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                4,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
            )
        migrated.query("SELECT name FROM sqlite_master WHERE type='table' AND name='pregnancy_record'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
        }
        migrated.query("SELECT reproductiveMode FROM cycle_profile WHERE id = 1").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("TRYING_TO_CONCEIVE")
        }
        migrated.query("SELECT pregnancyTestResult FROM ttc_day_log WHERE id = 't1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("POSITIVE")
        }
        migrated.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
