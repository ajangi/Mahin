package dev.mahin.backend.content

object ContentTypes {
    const val ARTICLE = "article"
    const val ENVELOPE = "envelope"
    const val PREGNANCY_WEEK = "pregnancy_week"
    const val DAILY_TIP = "daily_tip"
    const val PROGRAM = "program"
    const val COLLECTION = "collection"
    const val CARE_SCHEDULE = "care_schedule"
    const val CHECKLIST_TEMPLATE = "checklist_template"
    const val WARNING_SIGNS = "warning_signs"

    val WELLNESS_TYPES = setOf(DAILY_TIP, PROGRAM, COLLECTION)

    val PREGNANCY_COMPANION_TYPES = setOf(CARE_SCHEDULE, CHECKLIST_TEMPLATE, WARNING_SIGNS)

    fun isWellnessType(contentType: String): Boolean = contentType in WELLNESS_TYPES

    fun isPregnancyCompanionType(contentType: String): Boolean = contentType in PREGNANCY_COMPANION_TYPES

    fun requiresStructuredPayload(contentType: String): Boolean =
        isWellnessType(contentType) || isPregnancyCompanionType(contentType)
}
