package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity
import java.time.Instant

object ContentPublicationRules {
    fun isPublishedOnCatalog(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        now: Instant = Instant.now(),
    ): Boolean =
        document.publishedVersionId == version.id &&
            document.withdrawnAt == null &&
            version.status == "published" &&
            isWithinEffectiveWindow(version, now)

    fun isWithinEffectiveWindow(
        version: ContentVersionEntity,
        now: Instant = Instant.now(),
    ): Boolean {
        val from = version.effectiveFrom
        if (from != null && now.isBefore(from)) {
            return false
        }
        val to = version.effectiveTo
        return to == null || now.isBefore(to)
    }

    fun matchesDailyTipTargeting(
        payload: DailyTipPayload,
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ): Boolean {
        val targeting = payload.targeting
        val modeOk = targeting.modes.isEmpty() || (mode != null && mode in targeting.modes)
        val phaseOk =
            targeting.cyclePhases.isEmpty() ||
                (cyclePhase != null && cyclePhase in targeting.cyclePhases)
        val weekOk =
            targeting.pregnancyWeeks.isEmpty() ||
                (pregnancyWeek != null && pregnancyWeek in targeting.pregnancyWeeks)
        return modeOk && phaseOk && weekOk
    }
}
