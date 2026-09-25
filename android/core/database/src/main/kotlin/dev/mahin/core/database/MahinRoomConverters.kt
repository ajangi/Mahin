package dev.mahin.core.database

import androidx.room.TypeConverter
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate

class MahinRoomConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun reproductiveModeToString(value: ReproductiveMode): String = value.name

    @TypeConverter
    fun stringToReproductiveMode(value: String): ReproductiveMode = ReproductiveMode.valueOf(value)

    @TypeConverter
    fun cycleRegularityToString(value: CycleRegularity?): String? = value?.name

    @TypeConverter
    fun stringToCycleRegularity(value: String?): CycleRegularity? = value?.let { CycleRegularity.valueOf(it) }

    @TypeConverter
    fun flowLevelToString(value: PeriodFlowLevel?): String? = value?.name

    @TypeConverter
    fun stringToFlowLevel(value: String?): PeriodFlowLevel? = value?.let { PeriodFlowLevel.valueOf(it) }

    @TypeConverter
    fun ovulationTestToString(value: OvulationTestResult?): String? = value?.name

    @TypeConverter
    fun stringToOvulationTest(value: String?): OvulationTestResult? = value?.let { OvulationTestResult.valueOf(it) }

    @TypeConverter
    fun cervicalMucusToString(value: CervicalMucusType?): String? = value?.name

    @TypeConverter
    fun stringToCervicalMucus(value: String?): CervicalMucusType? = value?.let { CervicalMucusType.valueOf(it) }

    @TypeConverter
    fun pregnancyTestToString(value: PregnancyTestResult?): String? = value?.name

    @TypeConverter
    fun stringToPregnancyTest(value: String?): PregnancyTestResult? = value?.let { PregnancyTestResult.valueOf(it) }

    @TypeConverter
    fun pregnancyDatingSourceToString(value: PregnancyDatingSource?): String? = value?.name

    @TypeConverter
    fun stringToPregnancyDatingSource(value: String?): PregnancyDatingSource? =
        value?.let { PregnancyDatingSource.valueOf(it) }

    @TypeConverter
    fun pregnancyOutcomeToString(value: PregnancyOutcome?): String? = value?.name

    @TypeConverter
    fun stringToPregnancyOutcome(value: String?): PregnancyOutcome? = value?.let { PregnancyOutcome.valueOf(it) }

    @TypeConverter
    fun pregnancyAppointmentTypeToString(value: PregnancyAppointmentType?): String? = value?.name

    @TypeConverter
    fun stringToPregnancyAppointmentType(value: String?): PregnancyAppointmentType? =
        value?.let { PregnancyAppointmentType.valueOf(it) }
}
