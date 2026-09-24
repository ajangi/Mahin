package dev.mahin.core.database

import androidx.room.TypeConverter
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PeriodFlowLevel
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
}
