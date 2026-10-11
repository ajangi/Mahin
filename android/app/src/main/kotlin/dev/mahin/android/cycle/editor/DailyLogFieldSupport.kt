package dev.mahin.android.cycle.editor

import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.model.PainLocation

internal data class DailyLogFields(
    val moodTags: Set<String>,
    val symptomTags: Set<String>,
    val painSeverity: Int?,
    val painLocation: PainLocation?,
    val painNote: String?,
    val note: String?,
)

internal object DailyLogFieldSupport {
    fun parseTags(csv: String?): Set<String> =
        csv
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet() ?: emptySet()

    fun fromEntity(entity: DailyLogEntity?): DailyLogFields {
        if (entity == null) {
            return DailyLogFields(
                moodTags = emptySet(),
                symptomTags = emptySet(),
                painSeverity = null,
                painLocation = null,
                painNote = null,
                note = null,
            )
        }
        return DailyLogFields(
            moodTags = parseTags(entity.moodTags),
            symptomTags = parseTags(entity.symptomTags),
            painSeverity = entity.painSeverity,
            painLocation = entity.painLocation?.let { runCatching { PainLocation.valueOf(it) }.getOrNull() },
            painNote = entity.painNote,
            note = entity.note,
        )
    }
}
