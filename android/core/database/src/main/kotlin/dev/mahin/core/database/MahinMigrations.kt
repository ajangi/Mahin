package dev.mahin.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cycle_profile (
                    id INTEGER NOT NULL,
                    reproductiveMode TEXT NOT NULL,
                    typicalCycleLengthDays INTEGER,
                    typicalPeriodLengthDays INTEGER,
                    regularity TEXT NOT NULL,
                    onboardingCompleted INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS period_record (
                    id TEXT NOT NULL,
                    startDate TEXT NOT NULL,
                    endDate TEXT,
                    note TEXT,
                    createdAtEpochMs INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_period_record_startDate ON period_record(startDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_period_record_endDate ON period_record(endDate)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS period_day (
                    logDate TEXT NOT NULL,
                    flowLevel TEXT,
                    hasClots INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(logDate)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS daily_log (
                    id TEXT NOT NULL,
                    logDate TEXT NOT NULL,
                    moodTags TEXT NOT NULL,
                    symptomTags TEXT NOT NULL,
                    painSeverity INTEGER,
                    note TEXT,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_log_logDate ON daily_log(logDate)")
        }
    }
