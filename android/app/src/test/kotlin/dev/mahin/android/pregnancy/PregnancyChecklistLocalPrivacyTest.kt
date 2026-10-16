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
import dev.mahin.core.database.pregnancy.PregnancyChecklistRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.security.DisabledAppLockGateway
import dev.mahin.core.sync.InMemorySyncOutbox
import dev.mahin.domain.subscription.EntitlementTier
import kotlinx.coroutines.flow.first
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
    private lateinit var outbox: InMemorySyncOutbox

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        checklistRepository = PregnancyChecklistRepository(database)
        outbox = InMemorySyncOutbox()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun checklistMutations_leaveSyncOutboxEmpty() {
        runBlocking {
            checklistRepository.seedFromTemplate(
                listOf(
                    PregnancyChecklistRepository.TemplateItem(
                        templateId = "t1",
                        section = "مدارک",
                        label = "کارت ملی",
                        order = 1,
                    ),
                ),
            )
            val dao = database.pregnancyChecklistDao()
            val kind = PregnancyChecklistRepository.HOSPITAL_BAG_KIND
            val firstId =
                dao
                    .observeByKind(kind)
                    .first()
                    .first()
                    .id
            checklistRepository.setChecked(firstId, true)
            checklistRepository.addItem("لوازم", "شال")
            val customId =
                dao
                    .observeByKind(kind)
                    .first()
                    .first { it.label == "شال" }
                    .id
            checklistRepository.renameItem(customId, "شال گرم")
            checklistRepository.deleteItem(customId)
            assertThat(outbox.pending()).isEmpty()
        }
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
            checklistRepository.seedFromTemplate(
                listOf(
                    PregnancyChecklistRepository.TemplateItem(
                        templateId = "t1",
                        section = "مدارک",
                        label = "کارت ملی",
                        order = 1,
                    ),
                ),
            )
            val itemId =
                database
                    .pregnancyChecklistDao()
                    .observeByKind(PregnancyChecklistRepository.HOSPITAL_BAG_KIND)
                    .first()
                    .first()
                    .id
            checklistRepository.setChecked(itemId, true)
            checklistRepository.addItem("مدارک", "بیمه")
            val after = exportService.buildJsonExport() as HealthExportResult.Success
            val pattern = Regex(""""exportedAtEpochMs"\s*:\s*\d+""")
            assertThat(pattern.replace(after.json, """"exportedAtEpochMs":0"""))
                .isEqualTo(pattern.replace(before.json, """"exportedAtEpochMs":0"""))
            assertThat(outbox.pending()).isEmpty()
        }
    }
}
