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

    @Test
    fun migrate4To5_addsCustomSymptomsAndPreservesDailyLogs() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO daily_log (
                    id, logDate, moodTags, symptomTags, painSeverity, note, updatedAtEpochMs
                ) VALUES ('d1', '2025-01-01', 'آرام', 'سردرد', 3, NULL, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                5,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
            )
        migrated
            .query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='custom_symptom_tag'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated.query("PRAGMA table_info(daily_log)").use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(1))
            }
            assertThat(columns).contains("painLocation")
            assertThat(columns).contains("painNote")
        }
        migrated
            .query(
                "SELECT moodTags, symptomTags, note, painLocation, painNote FROM daily_log WHERE id = 'd1'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("آرام")
                assertThat(cursor.getString(1)).isEqualTo("سردرد")
                assertThat(cursor.isNull(2)).isTrue()
                assertThat(cursor.isNull(3)).isTrue()
                assertThat(cursor.isNull(4)).isTrue()
            }
        migrated.close()
    }

    @Test
    fun migrate5To6_addsMedicationTagsColumn() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO daily_log (
                    id, logDate, moodTags, symptomTags, painSeverity, painLocation, painNote, note, updatedAtEpochMs
                ) VALUES ('d1', '2025-01-01', '', 'سردرد', NULL, NULL, NULL, NULL, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                6,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
            )
        migrated.query("SELECT symptomTags, medicationTags FROM daily_log WHERE id = 'd1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("سردرد")
            assertThat(cursor.getString(1)).isEqualTo("")
        }
        migrated.close()
    }

    @Test
    fun migrate6To7_addsLocalCustomAndLifestyleColumns() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                """
                INSERT INTO daily_log (
                    id, logDate, moodTags, symptomTags, medicationTags, painSeverity,
                    painLocation, painNote, note, updatedAtEpochMs
                ) VALUES ('d1', '2025-01-01', '', 'سردرد', 'ویتامین', 3, 'HEAD', 'درد', 'یادداشت', 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                7,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )
        migrated
            .query(
                """
                SELECT symptomTags, localCustomSymptomTags, sleepLogged, exerciseLogged,
                    waterGlasses, stressLevel, painSeverity, medicationTags
                FROM daily_log WHERE id = 'd1'
                """.trimIndent(),
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("سردرد")
                assertThat(cursor.getString(1)).isEqualTo("")
                assertThat(cursor.getInt(2)).isEqualTo(0)
                assertThat(cursor.getInt(3)).isEqualTo(0)
                assertThat(cursor.getInt(4)).isEqualTo(0)
                assertThat(cursor.isNull(5)).isTrue()
                assertThat(cursor.getInt(6)).isEqualTo(3)
                assertThat(cursor.getString(7)).isEqualTo("ویتامین")
            }
        migrated.close()
    }

    @Test
    fun migrate7To8_addsWellnessContentTables() {
        helper.createDatabase(TEST_DB, 7).apply {
            execSQL(
                """
                INSERT INTO daily_log (
                    id, logDate, moodTags, symptomTags, medicationTags, painSeverity,
                    painLocation, painNote, note, localCustomSymptomTags, sleepLogged,
                    exerciseLogged, waterGlasses, stressLevel, updatedAtEpochMs
                ) VALUES ('d1', '2025-01-01', '', 'سردرد', '', NULL, NULL, NULL, NULL, '', 0, 0, 0, NULL, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                8,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
            )
        migrated
            .query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='content_article_cache'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated.query("SELECT symptomTags FROM daily_log WHERE id = 'd1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("سردرد")
        }
        migrated.close()
    }

    @Test
    fun migrate8To9_addsProgramCacheTable() {
        helper.createDatabase(TEST_DB, 8).apply {
            execSQL(
                """
                INSERT INTO guided_player_state (
                    id, programId, sessionId, stepIndex, stepElapsedMs, paused, updatedAtEpochMs
                ) VALUES (1, 'p', 's', 0, 0, 0, 0)
                """.trimIndent(),
            )
            close()
        }
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                9,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
            )
        migrated
            .query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='content_program_cache'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated
            .query("SELECT programId FROM guided_player_state WHERE id = 1")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("p")
            }
        migrated.close()
    }

    @Test
    fun migrate9To10_addsPregnancyChecklistTable() {
        helper.createDatabase(TEST_DB, 9).close()
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                10,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
            )
        migrated
            .query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='pregnancy_checklist_item'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated.execSQL(
            """
            INSERT INTO pregnancy_checklist_item
            (id, checklistKind, section, label, sortOrder, checked, templateItemId, updatedAtEpochMs)
            VALUES ('seed-1', 'hospital_bag', 'مدارک', 'کارت ملی', 1, 0, 't-seed', 1)
            """.trimIndent(),
        )
        migrated
            .query("SELECT label FROM pregnancy_checklist_item WHERE id = 'seed-1'")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("کارت ملی")
            }
        migrated.close()
    }

    @Test
    fun migrate10To11_addsPregnancyExpenseTable() {
        val v10 = helper.createDatabase(TEST_DB, 10)
        v10.execSQL(
            """
            INSERT INTO daily_log (
                id, logDate, moodTags, symptomTags, medicationTags, localCustomSymptomTags,
                painSeverity, painLocation, painNote, note, sleepLogged, exerciseLogged,
                waterGlasses, stressLevel, updatedAtEpochMs
            ) VALUES (
                'daily-seed', '2025-05-01', '', '[]', '', '', NULL, NULL, NULL, NULL, 0, 0, 0, NULL, 1
            )
            """.trimIndent(),
        )
        v10.execSQL(
            """
            INSERT INTO pregnancy_day_log
            (id, logDate, symptomTags, weightKg, bpSystolic, bpDiastolic, note, updatedAtEpochMs)
            VALUES ('preg-log-seed', '2025-05-01', '[]', 72.5, 120, 80, NULL, 1)
            """.trimIndent(),
        )
        v10.execSQL(
            """
            INSERT INTO pregnancy_checklist_item
            (id, checklistKind, section, label, sortOrder, checked, templateItemId, updatedAtEpochMs)
            VALUES ('chk-seed', 'hospital_bag', 'مدارک', 'کارت ملی', 1, 0, 't-seed', 1)
            """.trimIndent(),
        )
        v10.close()
        val migrated =
            helper.runMigrationsAndValidate(
                TEST_DB,
                11,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
            )
        migrated
            .query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='pregnancy_expense'",
            ).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated.execSQL(
            """
            INSERT INTO pregnancy_expense
            (id, category, amountMinor, currencyUnit, expenseDate, note, updatedAtEpochMs)
            VALUES ('exp-1', 'tests_imaging', 250000, 'toman', '2025-06-01', NULL, 1)
            """.trimIndent(),
        )
        migrated
            .query("SELECT amountMinor FROM pregnancy_expense WHERE id = 'exp-1'")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getLong(0)).isEqualTo(250_000L)
            }
        migrated
            .query("SELECT symptomTags FROM daily_log WHERE id = 'daily-seed'")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
            }
        migrated
            .query("SELECT bpSystolic FROM pregnancy_day_log WHERE id = 'preg-log-seed'")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getInt(0)).isEqualTo(120)
            }
        migrated
            .query("SELECT label FROM pregnancy_checklist_item WHERE id = 'chk-seed'")
            .use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(0)).isEqualTo("کارت ملی")
            }
        migrated.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
