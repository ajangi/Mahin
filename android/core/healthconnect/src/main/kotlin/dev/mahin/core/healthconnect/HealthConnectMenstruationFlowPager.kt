package dev.mahin.core.healthconnect

import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay

internal object HealthConnectMenstruationFlowPager {
    suspend fun readAllPages(
        readPage: suspend (pageToken: String?) -> HealthConnectMenstruationPage,
    ): List<HealthConnectMenstruationFlowDay> {
        val collected = mutableListOf<HealthConnectMenstruationFlowDay>()
        var pageToken: String? = null
        do {
            val page = readPage(pageToken)
            collected.addAll(page.records)
            pageToken = page.nextPageToken
        } while (!pageToken.isNullOrBlank())
        return collected
    }
}

data class HealthConnectMenstruationPage(
    val records: List<HealthConnectMenstruationFlowDay>,
    val nextPageToken: String?,
)
