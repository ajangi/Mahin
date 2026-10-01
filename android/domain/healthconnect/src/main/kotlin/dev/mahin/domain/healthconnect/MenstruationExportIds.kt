package dev.mahin.domain.healthconnect

import java.time.LocalDate

object MenstruationExportIds {
    const val CLIENT_RECORD_PREFIX = "mahin-period-day-"

    fun clientRecordId(localDate: LocalDate): String = "$CLIENT_RECORD_PREFIX$localDate"
}
