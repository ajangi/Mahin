package dev.mahin.android.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.premium.PremiumBillingCoordinator
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.BillingAdapterState
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.billing.EntitlementRepository
import dev.mahin.domain.subscription.CycleInsightsDailyLog
import dev.mahin.domain.subscription.CycleInsightsEngineV1
import dev.mahin.domain.subscription.CycleInsightsInput
import dev.mahin.domain.subscription.CycleInsightsPeriod
import dev.mahin.domain.subscription.CycleInsightsResult
import dev.mahin.domain.subscription.EntitlementRules
import dev.mahin.domain.subscription.PremiumFeature
import android.app.Activity
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CycleInsightsUiState(
    val loading: Boolean = true,
    val insights: CycleInsightsResult? = null,
    val hasPremium: Boolean = false,
    val showPaywall: Boolean = false,
    val billingState: BillingAdapterState = BillingAdapterState.Idle,
)

@HiltViewModel
class CycleInsightsViewModel
    @Inject
    constructor(
        private val cycleTrackingRepository: CycleTrackingRepository,
        private val entitlementRepository: EntitlementRepository,
        private val billingAdapter: BillingAdapter,
        private val premiumBillingCoordinator: PremiumBillingCoordinator,
    ) : ViewModel() {
        private val paywallVisible = MutableStateFlow(false)

        val uiState: StateFlow<CycleInsightsUiState> =
            combine(
                cycleTrackingRepository.observePeriods(),
                cycleTrackingRepository.observeDailyLogs(
                    LocalDate.now().minusMonths(3),
                    LocalDate.now(),
                ),
                entitlementRepository.entitlement,
                billingAdapter.state,
                paywallVisible,
            ) { periods, logs, entitlement, billingState, showPaywall ->
                val hasPremium =
                    EntitlementRules.canUse(entitlement, PremiumFeature.CYCLE_ADVANCED_TRENDS)
                val insights =
                    CycleInsightsEngineV1.build(
                        CycleInsightsInput(
                            periods = periods.map { it.toInsightsPeriod() },
                            recentLogs = logs.map { it.toInsightsLog() },
                            includePremiumSections = hasPremium,
                        ),
                    )
                CycleInsightsUiState(
                    loading = false,
                    insights = insights,
                    hasPremium = hasPremium,
                    showPaywall = showPaywall,
                    billingState = billingState,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CycleInsightsUiState(),
            )

        init {
            viewModelScope.launch {
                premiumBillingCoordinator.warmUp()
            }
        }

        fun openPaywall() {
            paywallVisible.value = true
        }

        fun dismissPaywall() {
            paywallVisible.value = false
        }

        fun priceLabel(productId: String): String? {
            val state = billingAdapter.state.value
            if (state is BillingAdapterState.Ready) {
                return state.products.firstOrNull { it.productId == productId }?.formattedPrice
            }
            return null
        }

        fun purchaseMonthly(activity: Activity) {
            viewModelScope.launch {
                premiumBillingCoordinator.purchaseMonthly(activity)
                dismissPaywall()
            }
        }

        fun purchaseAnnual(activity: Activity) {
            viewModelScope.launch {
                premiumBillingCoordinator.purchaseAnnual(activity)
                dismissPaywall()
            }
        }

        fun restorePurchases() {
            viewModelScope.launch {
                premiumBillingCoordinator.restorePurchases()
                dismissPaywall()
            }
        }

        private fun PeriodRecordEntity.toInsightsPeriod(): CycleInsightsPeriod =
            CycleInsightsPeriod(startDate = startDate, endDate = endDate)

        private fun DailyLogEntity.toInsightsLog(): CycleInsightsDailyLog =
            CycleInsightsDailyLog(
                logDate = logDate,
                symptomTags = symptomTags.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                moodTags = moodTags.split(',').map { it.trim() }.filter { it.isNotEmpty() },
            )
    }
