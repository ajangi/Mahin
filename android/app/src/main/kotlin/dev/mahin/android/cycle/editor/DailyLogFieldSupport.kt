package dev.mahin.android.cycle.editor

import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.model.PainLocation

internal data class DailyLogFields(
    val moodTags: Set<String>,
    val symptomTags: Set<String>,
    val medicationTags: Set<String>,
    val localCustomSymptomTags: Set<String>,
    val painSeverity: Int?,
    val painLocation: PainLocation?,
    val painNote: String?,
    val note: String?,
    val sleepLogged: Boolean,
    val exerciseLogged: Boolean,
    val waterGlasses: Int,
    val stressLevel: Int?,
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
                medicationTags = emptySet(),
                localCustomSymptomTags = emptySet(),
                painSeverity = null,
                painLocation = null,
                painNote = null,
                note = null,
                sleepLogged = false,
                exerciseLogged = false,
                waterGlasses = 0,
                stressLevel = null,
            )
        }
        return DailyLogFields(
            moodTags = parseTags(entity.moodTags),
            symptomTags = parseTags(entity.symptomTags),
            medicationTags = parseTags(entity.medicationTags),
            localCustomSymptomTags = parseTags(entity.localCustomSymptomTags),
            painSeverity = entity.painSeverity,
            painLocation = entity.painLocation?.let { runCatching { PainLocation.valueOf(it) }.getOrNull() },
            painNote = entity.painNote,
            note = entity.note,
            sleepLogged = entity.sleepLogged,
            exerciseLogged = entity.exerciseLogged,
            waterGlasses = entity.waterGlasses,
            stressLevel = entity.stressLevel,
        )
    }
}
