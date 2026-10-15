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
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.security.DisabledAppLockGateway
import dev.mahin.domain.subscription.EntitlementTier
import dev.mahin.core.database.entity.PregnancyChecklistItemEntity
import dev.mahin.core.database.pregnancy.PregnancyChecklistRepository
import dev.mahin.core.sync.SyncOutbox
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyChecklistLocalPrivacyTest {
    private lateinit var database: MahinDatabase
    private lateinit var checklistRepository: PregnancyChecklistRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        checklistRepository = PregnancyChecklistRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun checklistRepository_hasNoSyncOutboxDependency() {
        val params =
            PregnancyChecklistRepository::class.java.constructors.flatMap { it.parameterTypes.toList() }
        assertThat(params.any { it == SyncOutbox::class.java }).isFalse()
    }

    @Test
    fun checklistChanges_leaveExportUnchanged() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val cycleRepository = CycleTrackingRepository(database)
            val exportService =
                HealthExportService(
                    cycleRepository,
                    DisabledAppLockGateway,
                    EntitlementRepository(
                        SubscriptionPreferencesRepository(context),
                        dev.mahin.core.datastore.AccountSessionRepository(context),
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
                                dev.mahin.core.billing.GooglePlayBillingResponse(EntitlementTier.FREE.name, null)

                            override suspend fun restorePurchases(
                                authorization: String,
                                body: dev.mahin.core.billing.GooglePlayRestoreRequest,
                            ): dev.mahin.core.billing.GooglePlayBillingResponse =
                                dev.mahin.core.billing.GooglePlayBillingResponse(EntitlementTier.FREE.name, null)
                        },
                    ),
                )
            val before = exportService.buildJsonExport() as HealthExportResult.Success
            database.pregnancyChecklistDao().upsertAll(
                listOf(
                    PregnancyChecklistItemEntity(
                        id = "c1",
                        checklistKind = PregnancyChecklistRepository.HOSPITAL_BAG_KIND,
                        section = "مدارک",
                        label = "کارت ملی",
                        sortOrder = 1,
                        checked = true,
                        templateItemId = "t1",
                        updatedAtEpochMs = 1L,
                    ),
                ),
            )
            checklistRepository.setChecked("c1", true)
            val after = exportService.buildJsonExport() as HealthExportResult.Success
            val pattern = Regex(""""exportedAtEpochMs"\s*:\s*\d+""")
            assertThat(pattern.replace(after.json, """"exportedAtEpochMs":0"""))
                .isEqualTo(pattern.replace(before.json, """"exportedAtEpochMs":0"""))
        }
    }
}
