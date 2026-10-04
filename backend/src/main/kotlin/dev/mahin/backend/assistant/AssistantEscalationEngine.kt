package dev.mahin.backend.assistant

import dev.mahin.backend.content.PersianSearchNormalizer

/**
 * Deterministic pre-model safety layer.
 * Placeholder rules — require clinician sign-off before production enablement.
 */
object AssistantEscalationEngine {
    enum class Decision {
        PROCEED,
        ESCALATE_CRISIS,
        ESCALATE_URGENT,
        REFUSE_DIAGNOSIS,
        REFUSE_PRESCRIPTION,
        REFUSE_NO_GROUNDING,
    }

    data class Result(
        val decision: Decision,
        val code: String? = null,
    )

    fun evaluate(
        question: String,
        retrievedChunks: List<RetrievedContentChunk>,
    ): Result {
        val normalized = PersianSearchNormalizer.normalize(question)
        return when {
            CRISIS_TERMS.any { normalized.contains(PersianSearchNormalizer.normalize(it)) } ->
                Result(Decision.ESCALATE_CRISIS, "crisis_placeholder_needs_clinical_signoff")
            URGENT_TERMS.any { normalized.contains(PersianSearchNormalizer.normalize(it)) } ->
                Result(Decision.ESCALATE_URGENT, "urgent_placeholder_needs_clinical_signoff")
            DIAGNOSIS_TERMS.any { normalized.contains(PersianSearchNormalizer.normalize(it)) } ->
                Result(Decision.REFUSE_DIAGNOSIS, "no_diagnosis_placeholder_needs_clinical_signoff")
            PRESCRIPTION_TERMS.any { normalized.contains(PersianSearchNormalizer.normalize(it)) } ->
                Result(Decision.REFUSE_PRESCRIPTION, "no_prescription_placeholder_needs_clinical_signoff")
            retrievedChunks.isEmpty() ->
                Result(Decision.REFUSE_NO_GROUNDING, "no_approved_grounding")
            else -> Result(Decision.PROCEED)
        }
    }

    // PLACEHOLDER — clinician review required before production.
    private val CRISIS_TERMS =
        listOf(
            "خودکشی",
            "میخواهم بمیرم",
            "می‌خواهم بمیرم",
            "دیگه نمیخوام زنده باشم",
        )

    private val URGENT_TERMS =
        listOf(
            "خونریزی شدید",
            "درد شدید ناگهانی",
            "تب خیلی بالا",
        )

    private val DIAGNOSIS_TERMS =
        listOf(
            "تشخیص",
            "آیا باردارم",
            "سرطان دارم",
            "بیماری دارم",
        )

    private val PRESCRIPTION_TERMS =
        listOf(
            "دوز",
            "تجویز",
            "چند قرص",
            "نسخه",
        )
}
