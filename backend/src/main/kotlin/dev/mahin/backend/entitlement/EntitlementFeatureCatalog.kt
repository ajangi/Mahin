package dev.mahin.backend.entitlement

object EntitlementFeatureCatalog {
    const val CYCLE_BASIC_INSIGHTS = "cycle_basic_insights"
    const val CYCLE_ADVANCED_TRENDS = "cycle_advanced_trends"
    const val CYCLE_CORRELATION_OBSERVATIONS = "cycle_correlation_observations"
    const val EXPORT_EXTENDED_LAYOUT = "export_extended_layout"

    fun featuresFor(tier: EntitlementTier): List<String> =
        when (tier) {
            EntitlementTier.FREE ->
                listOf(
                    CYCLE_BASIC_INSIGHTS,
                )
            EntitlementTier.PREMIUM_MONTHLY,
            EntitlementTier.PREMIUM_ANNUAL,
            ->
                listOf(
                    CYCLE_BASIC_INSIGHTS,
                    CYCLE_ADVANCED_TRENDS,
                    CYCLE_CORRELATION_OBSERVATIONS,
                    EXPORT_EXTENDED_LAYOUT,
                )
        }

    fun isPremium(tier: EntitlementTier): Boolean = tier != EntitlementTier.FREE
}
