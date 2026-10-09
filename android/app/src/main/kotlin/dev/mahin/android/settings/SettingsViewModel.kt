package dev.mahin.android.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.premium.PremiumBillingCoordinator
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.BillingAdapterState
import dev.mahin.core.billing.EntitlementRepository
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.subscription.EntitlementTier
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val hasActivePregnancy: Boolean = false,
    val showPregnancyStartSheet: Boolean = false,
    val modeChangeBlockedMessage: Boolean = false,
    val postPregnancyTransition: Boolean = false,
    val healthConnectEntryVisible: Boolean = false,
    val healthAssistantEntryVisible: Boolean = false,
    val showPaywall: Boolean = false,
    val billingState: BillingAdapterState = BillingAdapterState.Idle,
    val premiumActive: Boolean = false,
    val showPremiumPaywallEntry: Boolean = true,
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val featureFlagGateway: FeatureFlagGateway,
        private val featureFlagRepository: FeatureFlagRepository,
        private val billingAdapter: BillingAdapter,
        private val premiumBillingCoordinator: PremiumBillingCoordinator,
        private val entitlementRepository: EntitlementRepository,
    ) : ViewModel() {
        private val paywallVisible = MutableStateFlow(false)
        private var paywallWarmUpJob: Job? = null
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                refreshFeatureFlags()
            }
            viewModelScope.launch {
                combine(
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { profile, pregnancy ->
                    profile to pregnancy
                }.collect { (profile, pregnancy) ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    _uiState.update {
                        it.copy(
                            reproductiveMode = mode,
                            hasActivePregnancy = pregnancy != null && mode == ReproductiveMode.PREGNANT,
                            postPregnancyTransition = mode == ReproductiveMode.POST_PREGNANCY_TRANSITION,
                        )
                    }
                }
            }
            viewModelScope.launch {
                combine(paywallVisible, billingAdapter.state) { showPaywall, billingState ->
                    showPaywall to billingState
                }.collect { (showPaywall, billingState) ->
                    _uiState.update {
                        it.copy(showPaywall = showPaywall, billingState = billingState)
                    }
                }
            }
            viewModelScope.launch {
                entitlementRepository.entitlement.collect { snapshot ->
                    val premium = snapshot.tier != EntitlementTier.FREE
                    _uiState.update {
                        it.copy(
                            premiumActive = premium,
                            showPremiumPaywallEntry = !premium,
                        )
                    }
                }
            }
        }

        private fun refreshFeatureFlags() {
            _uiState.update {
                it.copy(
                    healthConnectEntryVisible =
                        featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT),
                    healthAssistantEntryVisible =
                        featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT),
                )
            }
        }

        fun onReproductiveModeSelected(mode: ReproductiveMode) {
            viewModelScope.launch {
                val state = _uiState.value
                if (state.hasActivePregnancy && mode != ReproductiveMode.PREGNANT) {
                    _uiState.update { it.copy(modeChangeBlockedMessage = true) }
                    return@launch
                }
                _uiState.update { it.copy(modeChangeBlockedMessage = false) }
                when (mode) {
                    ReproductiveMode.PREGNANT -> {
                        if (state.hasActivePregnancy) {
                            pregnancyRepository.updateReproductiveMode(ReproductiveMode.PREGNANT)
                        } else {
                            _uiState.update { it.copy(showPregnancyStartSheet = true) }
                        }
                    }
                    ReproductiveMode.CYCLE_TRACKING,
                    ReproductiveMode.TRYING_TO_CONCEIVE,
                    -> pregnancyRepository.updateReproductiveMode(mode)
                    else -> Unit
                }
            }
        }

        fun dismissPregnancyStartSheet() {
            _uiState.update { it.copy(showPregnancyStartSheet = false) }
        }

        fun confirmPregnancyStart(
            lmpDate: LocalDate,
            clinicalEdd: LocalDate?,
        ) {
            viewModelScope.launch {
                pregnancyRepository.startPregnancy(
                    lmpDate = lmpDate,
                    clinicalEddDate = clinicalEdd,
                    datingReason = null,
                )
                _uiState.update { it.copy(showPregnancyStartSheet = false) }
            }
        }

        fun resumeCycleTracking() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.CYCLE_TRACKING)
            }
        }

        fun resumeTtc() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.TRYING_TO_CONCEIVE)
            }
        }

        fun openPaywall() {
            if (paywallVisible.value) return
            paywallVisible.value = true
            if (paywallWarmUpJob?.isActive == true) return
            val job =
                viewModelScope.launch {
                    try {
                        premiumBillingCoordinator.warmUp()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        // Billing warm-up is best-effort; paywall stays open.
                    } finally {
                        if (paywallWarmUpJob === this.coroutineContext[kotlinx.coroutines.Job]) {
                            paywallWarmUpJob = null
                        }
                    }
                }
            paywallWarmUpJob = job
        }

        fun dismissPaywall() {
            paywallWarmUpJob?.cancel()
            paywallWarmUpJob = null
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

        override fun onCleared() {
            dismissPaywall()
            super.onCleared()
        }
    }
