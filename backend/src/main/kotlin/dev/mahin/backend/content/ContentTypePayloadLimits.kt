package dev.mahin.backend.content

object ContentTypePayloadLimits {
    const val MAX_PAYLOAD_BYTES = 65_536
    const val MAX_STRING_CHARS = 2_000
    const val MAX_SESSIONS = 50
    const val MAX_STEPS_PER_SESSION = 100
    const val MAX_COLLECTION_ARTICLES = 200
    const val MAX_CARE_SCHEDULE_ITEMS = 120
    const val MAX_CHECKLIST_ITEMS = 200
}
