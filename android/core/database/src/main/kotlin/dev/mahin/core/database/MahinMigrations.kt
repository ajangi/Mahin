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

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS ttc_day_log (
                    id TEXT NOT NULL,
                    logDate TEXT NOT NULL,
                    bbtCelsius REAL,
                    ovulationTestResult TEXT,
                    cervicalMucus TEXT,
                    intercourseLogged INTEGER NOT NULL,
                    intercourseProtected INTEGER,
                    pregnancyTestResult TEXT,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ttc_day_log_logDate ON ttc_day_log(logDate)")
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        @Suppress("LongMethod")
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pregnancy_record (
                    id TEXT NOT NULL,
                    lmpDate TEXT NOT NULL,
                    clinicalEddDate TEXT,
                    effectiveEddDate TEXT NOT NULL,
                    datingSource TEXT NOT NULL,
                    isActive INTEGER NOT NULL,
                    outcome TEXT,
                    outcomeRecordedAtEpochMs INTEGER,
                    suppressCelebratoryNotifications INTEGER NOT NULL,
                    wantsSupportContent INTEGER,
                    createdAtEpochMs INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pregnancy_dating_revision (
                    id TEXT NOT NULL,
                    pregnancyId TEXT NOT NULL,
                    effectiveEddDate TEXT NOT NULL,
                    datingSource TEXT NOT NULL,
                    reason TEXT,
                    changedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_pregnancy_dating_revision_pregnancyId " +
                    "ON pregnancy_dating_revision(pregnancyId)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pregnancy_day_log (
                    id TEXT NOT NULL,
                    logDate TEXT NOT NULL,
                    symptomTags TEXT NOT NULL,
                    weightKg REAL,
                    bpSystolic INTEGER,
                    bpDiastolic INTEGER,
                    note TEXT,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_pregnancy_day_log_logDate " +
                    "ON pregnancy_day_log(logDate)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pregnancy_appointment (
                    id TEXT NOT NULL,
                    pregnancyId TEXT NOT NULL,
                    appointmentType TEXT NOT NULL,
                    title TEXT NOT NULL,
                    scheduledAtEpochMs INTEGER NOT NULL,
                    location TEXT,
                    clinicianName TEXT,
                    note TEXT,
                    reminderEnabled INTEGER NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_pregnancy_appointment_pregnancyId " +
                    "ON pregnancy_appointment(pregnancyId)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS kick_session (
                    id TEXT NOT NULL,
                    pregnancyId TEXT NOT NULL,
                    startedAtEpochMs INTEGER NOT NULL,
                    endedAtEpochMs INTEGER,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_kick_session_pregnancyId ON kick_session(pregnancyId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS kick_event (
                    id TEXT NOT NULL,
                    sessionId TEXT NOT NULL,
                    recordedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_kick_event_sessionId ON kick_event(sessionId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS contraction_session (
                    id TEXT NOT NULL,
                    pregnancyId TEXT NOT NULL,
                    startedAtEpochMs INTEGER NOT NULL,
                    endedAtEpochMs INTEGER,
                    updatedAtEpochMs INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_contraction_session_pregnancyId " +
                    "ON contraction_session(pregnancyId)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS contraction_event (
                    id TEXT NOT NULL,
                    sessionId TEXT NOT NULL,
                    startedAtEpochMs INTEGER NOT NULL,
                    endedAtEpochMs INTEGER,
                    PRIMARY KEY(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contraction_event_sessionId ON contraction_event(sessionId)")
        }
    }
