package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import java.time.LocalDate

@Entity(
    tableName = "ttc_day_log",
    indices = [Index(value = ["logDate"], unique = true)],
)
data class TtcDayLogEntity(
    @PrimaryKey val id: String,
    val logDate: LocalDate,
    val bbtCelsius: Double?,
    val ovulationTestResult: OvulationTestResult?,
    val cervicalMucus: CervicalMucusType?,
    val intercourseLogged: Boolean,
    val intercourseProtected: Boolean?,
    val pregnancyTestResult: PregnancyTestResult?,
    val updatedAtEpochMs: Long,
)
