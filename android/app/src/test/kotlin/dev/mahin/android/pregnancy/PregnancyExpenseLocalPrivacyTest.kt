package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.billing.EntitlementRepository
import dev.mahin.core.billing.HealthExportResult
import dev.mahin.core.billing.HealthExportService
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyExpenseRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.security.DisabledAppLockGateway
import dev.mahin.core.sync.InMemorySyncOutbox
import dev.mahin.domain.subscription.EntitlementTier
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyExpenseLocalPrivacyTest {
    private lateinit var database: MahinDatabase
    private lateinit var expenseRepository: PregnancyExpenseRepository
    private lateinit var outbox: InMemorySyncOutbox

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        expenseRepository = PregnancyExpenseRepository(database)
        outbox = InMemorySyncOutbox()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun expenseMutations_leaveSyncOutboxEmpty() {
        runBlocking {
            expenseRepository.upsertExpense(
                id = "exp-local-1",
                category = "tests_imaging",
                amountMinor = 999_999,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 6, 1),
                note = "secret note",
            )
            expenseRepository.deleteExpense("exp-local-1")
            assertThat(outbox.pending()).isEmpty()
        }
    }

    @Test
    fun expenseChanges_leaveExportUnchanged() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val cycleRepository = CycleTrackingRepository(database)
            val exportService =
                HealthExportService(
                    cycleRepository,
                    DisabledAppLockGateway,
                    EntitlementRepository(
                        SubscriptionPreferencesRepository(context),
                        dev.mahin.core.datastore
                            .AccountSessionRepository(context),
                        object : dev.mahin.core.billing.EntitlementApi {
                            override suspend fun currentEntitlement(
                                authorization: String,
                            ): dev.mahin.core.billing.EntitlementApiResponse =
                                dev.mahin.core.billing.EntitlementApiResponse(
                                    EntitlementTier.FREE.name,
                                    null,
                                    emptyList(),
                                    "2026-01-01T00:00:00Z",
                                )

                            override suspend fun verifyPurchase(
                                authorization: String,
                                body: dev.mahin.core.billing.GooglePlayVerifyRequest,
                            ): dev.mahin.core.billing.GooglePlayBillingResponse =
                                dev.mahin.core.billing
                                    .GooglePlayBillingResponse(EntitlementTier.FREE.name, null)

                            override suspend fun restorePurchases(
                                authorization: String,
                                body: dev.mahin.core.billing.GooglePlayRestoreRequest,
                            ): dev.mahin.core.billing.GooglePlayBillingResponse =
                                dev.mahin.core.billing
                                    .GooglePlayBillingResponse(EntitlementTier.FREE.name, null)
                        },
                    ),
                )
            val before = exportService.buildJsonExport() as HealthExportResult.Success
            expenseRepository.upsertExpense(
                id = null,
                category = "hospital_birth",
                amountMinor = 50_000_000,
                currencyUnit = "toman",
                expenseDate = LocalDate.of(2025, 7, 1),
                note = "must not export",
            )
            val after = exportService.buildJsonExport() as HealthExportResult.Success
            assertThat(after.json).doesNotContain("must not export")
            assertThat(after.json).doesNotContain("50000000")
            val pattern = Regex(""""exportedAtEpochMs"\s*:\s*\d+""")
            assertThat(pattern.replace(after.json, """"exportedAtEpochMs":0"""))
                .isEqualTo(pattern.replace(before.json, """"exportedAtEpochMs":0"""))
            assertThat(outbox.pending()).isEmpty()
        }
    }
}
