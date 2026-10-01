package dev.mahin.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.MenstruationFlowRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlow
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay
import dev.mahin.domain.healthconnect.MenstruationFlowMapper
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidHealthConnectClientGateway
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : HealthConnectClientGateway {
        private val zoneId: ZoneId = ZoneId.systemDefault()

        override suspend fun availability(): HealthConnectAvailability =
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.SDK_UNAVAILABLE
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                    HealthConnectAvailability.UPDATE_REQUIRED
                HealthConnectClient.SDK_AVAILABLE -> {
                    val client = runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
                    if (client == null) {
                        HealthConnectAvailability.NOT_INSTALLED
                    } else {
                        HealthConnectAvailability.READY
                    }
                }
                else -> HealthConnectAvailability.SDK_UNAVAILABLE
            }

        override suspend fun grantedPermissionStrings(): Set<String> {
            val client = HealthConnectClient.getOrCreate(context)
            return client.permissionController.getGrantedPermissions()
        }

        override suspend fun importMenstruationFlowDays(): List<HealthConnectMenstruationFlowDay> {
            val client = HealthConnectClient.getOrCreate(context)
            val end = Instant.now()
            val start = end.minusSeconds(86400L * 730)
            val response =
                client.readRecords(
                    ReadRecordsRequest(
                        recordType = MenstruationFlowRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(start, end),
                    ),
                )
            return response.records.mapNotNull { record -> record.toDomain(zoneId) }
        }

        override suspend fun exportMenstruationFlowDays(days: List<HealthConnectMenstruationFlowDay>): Int {
            if (days.isEmpty()) return 0
            val client = HealthConnectClient.getOrCreate(context)
            val records =
                days.map { day ->
                    MenstruationFlowRecord(
                        time = MenstruationFlowMapper.localDateToInstantStartOfDay(day.localDate, zoneId),
                        zoneOffset = zoneId.rules.getOffset(day.localDate.atStartOfDay()),
                        flow = day.flow.toSdkFlowInt(),
                        metadata = manualExportMetadata(),
                    )
                }
            client.insertRecords(records)
            return records.size
        }

        private fun manualExportMetadata(): Metadata =
            Metadata(
                id = "",
                dataOrigin = DataOrigin(context.packageName),
                lastModifiedTime = Instant.now(),
                clientRecordId = null,
                clientRecordVersion = 0L,
                device =
                    Device(
                        manufacturer = "",
                        model = "",
                        type = Device.TYPE_UNKNOWN,
                    ),
                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
            )

        private fun MenstruationFlowRecord.toDomain(zoneId: ZoneId): HealthConnectMenstruationFlowDay? {
            val flow = flow.toDomainFlow() ?: return null
            val localDate = time.atZone(zoneId).toLocalDate()
            return HealthConnectMenstruationFlowDay(
                localDate = localDate,
                flow = flow,
                sourceUpdatedAt = metadata.lastModifiedTime,
            )
        }

        private fun Int.toDomainFlow(): HealthConnectMenstruationFlow? =
            when (this) {
                MenstruationFlowRecord.FLOW_UNKNOWN -> HealthConnectMenstruationFlow.UNKNOWN
                MenstruationFlowRecord.FLOW_LIGHT -> HealthConnectMenstruationFlow.LIGHT
                MenstruationFlowRecord.FLOW_MEDIUM -> HealthConnectMenstruationFlow.MEDIUM
                MenstruationFlowRecord.FLOW_HEAVY -> HealthConnectMenstruationFlow.HEAVY
                else -> null
            }

        private fun HealthConnectMenstruationFlow.toSdkFlowInt(): Int =
            when (this) {
                HealthConnectMenstruationFlow.UNKNOWN -> MenstruationFlowRecord.FLOW_UNKNOWN
                HealthConnectMenstruationFlow.LIGHT -> MenstruationFlowRecord.FLOW_LIGHT
                HealthConnectMenstruationFlow.MEDIUM -> MenstruationFlowRecord.FLOW_MEDIUM
                HealthConnectMenstruationFlow.HEAVY -> MenstruationFlowRecord.FLOW_HEAVY
            }
    }
