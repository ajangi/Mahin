package dev.mahin.backend.content

object ContentTypes {
    const val ARTICLE = "article"
    const val ENVELOPE = "envelope"
    const val PREGNANCY_WEEK = "pregnancy_week"
    const val DAILY_TIP = "daily_tip"
    const val PROGRAM = "program"
    const val COLLECTION = "collection"

    val WELLNESS_TYPES = setOf(DAILY_TIP, PROGRAM, COLLECTION)

    fun isWellnessType(contentType: String): Boolean = contentType in WELLNESS_TYPES
}
