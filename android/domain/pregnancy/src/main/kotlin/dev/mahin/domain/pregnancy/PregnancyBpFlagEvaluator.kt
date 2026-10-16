package dev.mahin.domain.pregnancy

/**
 * BP flagging uses caller-supplied thresholds from reviewed CMS/config only.
 * This module must not embed clinical threshold constants.
 */
data class PregnancyBpReading(
    val systolic: Int,
    val diastolic: Int,
)

data class PregnancyBpThresholds(
    val elevatedSystolic: Int,
    val elevatedDiastolic: Int,
    val urgentSystolic: Int,
    val urgentDiastolic: Int,
)

enum class PregnancyBpFlagLevel {
    NONE,
    ELEVATED,
    URGENT,
}

object PregnancyBpFlagEvaluator {
    fun evaluate(
        reading: PregnancyBpReading?,
        thresholds: PregnancyBpThresholds?,
    ): PregnancyBpFlagLevel {
        if (reading == null || thresholds == null) {
            return PregnancyBpFlagLevel.NONE
        }
        val urgent =
            reading.systolic >= thresholds.urgentSystolic ||
                reading.diastolic >= thresholds.urgentDiastolic
        if (urgent) {
            return PregnancyBpFlagLevel.URGENT
        }
        val elevated =
            reading.systolic >= thresholds.elevatedSystolic ||
                reading.diastolic >= thresholds.elevatedDiastolic
        if (elevated) {
            return PregnancyBpFlagLevel.ELEVATED
        }
        return PregnancyBpFlagLevel.NONE
    }
}
