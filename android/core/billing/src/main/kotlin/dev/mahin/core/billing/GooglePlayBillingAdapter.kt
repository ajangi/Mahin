package dev.mahin.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.domain.subscription.EntitlementTier
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class GooglePlayBillingAdapter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : BillingAdapter,
        PurchasesUpdatedListener {
        private val mutex = Mutex()
        private val _state = MutableStateFlow<BillingAdapterState>(BillingAdapterState.Idle)
        override val state: StateFlow<BillingAdapterState> = _state.asStateFlow()

        private var pendingPurchaseContinuation: ((BillingPurchaseReceipt?) -> Unit)? = null
        private var productDetailsById: Map<String, ProductDetails> = emptyMap()

        private val billingClient: BillingClient =
            BillingClient
                .newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()

        override suspend fun startConnection() {
            mutex.withLock {
                if (billingClient.isReady) {
                    refreshProducts()
                    return
                }
                _state.value = BillingAdapterState.Connecting
                val connected = billingClient.connectAwait()
                if (!connected) {
                    _state.value = BillingAdapterState.Error("billing_unavailable")
                    return
                }
                refreshProducts()
            }
        }

        override suspend fun refreshProducts() {
            if (!billingClient.isReady) return
            val productList =
                BillingProductIds.subscriptionSkus.map { sku ->
                    QueryProductDetailsParams.Product
                        .newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            val params =
                QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(productList)
                    .build()
            val result = billingClient.queryProductDetails(params)
            val details = result.productDetailsList.orEmpty()
            productDetailsById = details.associateBy { it.productId }
            val offers =
                details.map { detail ->
                    val formatted =
                        detail.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                    BillingProductOffer(detail.productId, formatted)
                }
            _state.value = BillingAdapterState.Ready(offers)
        }

        override suspend fun restorePurchases(): List<BillingPurchaseReceipt> {
            if (!billingClient.isReady) {
                awaitReady()
            }
            val params =
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            val purchasesResult = billingClient.queryPurchasesAsync(params)
            return purchasesResult.purchasesList.mapNotNull { purchase -> purchase.toReceipt() }
        }

        override suspend fun launchSubscriptionPurchase(
            activityHost: Any,
            productId: String,
        ): BillingPurchaseReceipt? {
            val activity = activityHost as? Activity ?: return null
            if (!billingClient.isReady) {
                awaitReady()
            }
            val details = productDetailsById[productId] ?: return null
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return null
            val productParams =
                BillingFlowParams.ProductDetailsParams
                    .newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offerToken)
                    .build()
            val flowParams =
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(listOf(productParams))
                    .build()
            return suspendCancellableCoroutine { cont ->
                pendingPurchaseContinuation = { receipt ->
                    if (cont.isActive) {
                        cont.resumeWith(Result.success(receipt))
                    }
                }
                val launchResult = billingClient.launchBillingFlow(activity, flowParams)
                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    pendingPurchaseContinuation = null
                    cont.resumeWith(Result.success(null))
                }
            }
        }

        override fun onPurchasesUpdated(
            billingResult: BillingResult,
            purchases: MutableList<Purchase>?,
        ) {
            val continuation = pendingPurchaseContinuation
            pendingPurchaseContinuation = null
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
                continuation?.invoke(null)
                return
            }
            val purchase = purchases.first()
            acknowledgeIfNeeded(purchase)
            continuation?.invoke(purchase.toReceipt())
        }

        private fun acknowledgeIfNeeded(purchase: Purchase) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED || purchase.isAcknowledged) {
                return
            }
            val params =
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            billingClient.acknowledgePurchase(params) { }
        }

        private fun Purchase.toReceipt(): BillingPurchaseReceipt? {
            val sku = products.firstOrNull() ?: return null
            val tier =
                when (sku) {
                    BillingProductIds.PREMIUM_MONTHLY -> EntitlementTier.PREMIUM_MONTHLY
                    BillingProductIds.PREMIUM_ANNUAL -> EntitlementTier.PREMIUM_ANNUAL
                    else -> return null
                }
            if (purchaseState != Purchase.PurchaseState.PURCHASED) return null
            return BillingPurchaseReceipt(
                productId = sku,
                purchaseToken = purchaseToken,
                tier = tier,
            )
        }

        private suspend fun awaitReady() {
            if (billingClient.isReady) return
            startConnection()
        }

        private suspend fun BillingClient.connectAwait(): Boolean =
            suspendCancellableCoroutine { cont ->
                val resumed = AtomicBoolean(false)
                fun tryResume(value: Boolean) {
                    if (resumed.compareAndSet(false, true) && cont.isActive) {
                        cont.resumeWith(Result.success(value))
                    }
                }
                startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            tryResume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                        }

                        override fun onBillingServiceDisconnected() {
                            tryResume(false)
                        }
                    },
                )
            }
    }
