package dev.mahin.core.analytics

data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
)

interface AnalyticsGateway {
    fun track(event: AnalyticsEvent)
}

object NoOpAnalyticsGateway : AnalyticsGateway {
    override fun track(event: AnalyticsEvent) = Unit
}
