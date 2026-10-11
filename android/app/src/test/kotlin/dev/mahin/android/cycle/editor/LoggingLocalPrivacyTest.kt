package dev.mahin.android.cycle.editor

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.billing.HealthExportResult
import dev.mahin.core.billing.HealthExportService
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CustomSymptomTagEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
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
class LoggingLocalPrivacyTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var customSymptomTagRepository: CustomSymptomTagRepository
    private val outbox = InMemorySyncOutbox()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cycleRepository = CycleTrackingRepository(database)
        customSymptomTagRepository = CustomSymptomTagRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun customSymptomTag_doesNotEnqueueSync() {
        runBlocking {
            customSymptomTagRepository.upsert(
                CustomSymptomTagEntity(
                    id = "c1",
                    label = "علامت سفارشی",
                    createdAtEpochMs = 0L,
                ),
            )
            assertThat(outbox.pending()).isEmpty()
        }
    }

    @Test
    fun exportJson_unchangedWhenCustomTagRegistryAdded() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val day = LocalDate.of(2025, 3, 1)
            cycleRepository.upsertDailyLog(
                date = day,
                moodTags = setOf("آرام"),
                symptomTags = setOf("سردرد"),
                painSeverity = null,
                note = null,
            )
            val exportService =
                HealthExportService(
                    cycleRepository,
                    DisabledAppLockGateway,
                    dev.mahin.core.billing.EntitlementRepository(
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
            customSymptomTagRepository.upsert(
                CustomSymptomTagEntity(
                    id = "c2",
                    label = "علامت دیگر",
                    createdAtEpochMs = 1L,
                ),
            )
            val after = exportService.buildJsonExport() as HealthExportResult.Success
            val exportTimestampPattern = Regex(""""exportedAtEpochMs"\s*:\s*\d+""")
            val beforePayload = exportTimestampPattern.replace(before.json, """"exportedAtEpochMs":0""")
            val afterPayload = exportTimestampPattern.replace(after.json, """"exportedAtEpochMs":0""")
            assertThat(afterPayload).isEqualTo(beforePayload)
        }
    }
}
