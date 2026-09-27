package dev.mahin.core.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.domain.subscription.EntitlementTier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EntitlementRepositoryTest {
    private lateinit var context: Context
    private lateinit var subscriptionPreferencesRepository: SubscriptionPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        subscriptionPreferencesRepository = SubscriptionPreferencesRepository(context)
    }

    @Test
    fun applyPlayPurchaseCachesPremiumLocally() =
        runTest {
            val repository =
                EntitlementRepository(
                    subscriptionPreferencesRepository,
                    AccountSessionRepository(context),
                    FakeEntitlementApi(),
                )
            repository.applyPlayPurchase(
                BillingPurchaseReceipt(
                    productId = BillingProductIds.PREMIUM_MONTHLY,
                    purchaseToken = "local-token",
                    tier = EntitlementTier.PREMIUM_MONTHLY,
                ),
            )
            val snapshot = repository.entitlement.first()
            assertThat(snapshot.tier).isEqualTo(EntitlementTier.PREMIUM_MONTHLY)
        }

    private class FakeEntitlementApi : EntitlementApi {
        override suspend fun currentEntitlement(authorization: String): EntitlementApiResponse =
            EntitlementApiResponse("FREE", null, emptyList(), "2026-01-01T00:00:00Z")

        override suspend fun verifyPurchase(
            authorization: String,
            body: GooglePlayVerifyRequest,
        ): GooglePlayBillingResponse = GooglePlayBillingResponse("PREMIUM_MONTHLY", null)

        override suspend fun restorePurchases(
            authorization: String,
            body: GooglePlayRestoreRequest,
        ): GooglePlayBillingResponse = GooglePlayBillingResponse("FREE", null)
    }
}
