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
import dev.mahin.domain.healthconnect.HealthConnectAvailability
import dev.mahin.domain.healthconnect.HealthConnectClientResult
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlow
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay
import dev.mahin.domain.healthconnect.HealthConnectRemoteClient
import dev.mahin.domain.healthconnect.MenstruationExportIds
import dev.mahin.domain.healthconnect.MenstruationFlowExportWrite
import dev.mahin.domain.healthconnect.MenstruationFlowMapper
import dev.mahin.domain.healthconnect.MenstruationImportPolicy
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private typealias MenstruationFlowDaysResult =
    HealthConnectClientResult<List<HealthConnectMenstruationFlowDay>>

@Singleton
class AndroidHealthConnectRemoteClient
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : HealthConnectRemoteClient {
        private val exportZoneId: ZoneId = ZoneId.systemDefault()

        override suspend fun availability(): HealthConnectAvailability =
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.SDK_UNAVAILABLE
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                    HealthConnectAvailability.UPDATE_REQUIRED
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (runCatching { HealthConnectClient.getOrCreate(context) }.isSuccess) {
                        HealthConnectAvailability.READY
                    } else {
                        HealthConnectAvailability.NOT_INSTALLED
                    }
                }
                else -> HealthConnectAvailability.SDK_UNAVAILABLE
            }

        override suspend fun grantedPermissionStrings(): HealthConnectClientResult<Set<String>> {
            val client = clientOrNull() ?: return HealthConnectClientResult.NotReady
            return runCatching {
                HealthConnectClientResult.Ok(client.permissionController.getGrantedPermissions())
            }.getOrElse { HealthConnectClientResult.Error(it.javaClass.simpleName) }
        }

        override suspend fun readMenstruationFlowDays(): MenstruationFlowDaysResult {
            val client = clientOrNull() ?: return HealthConnectClientResult.NotReady
            return runCatching {
                val end = Instant.now()
                val start = end.minusSeconds(86400L * 730)
                val collected =
                    HealthConnectMenstruationFlowPager.readAllPages { pageToken ->
                        val response =
                            client.readRecords(
                                ReadRecordsRequest(
                                    recordType = MenstruationFlowRecord::class,
                                    timeRangeFilter = TimeRangeFilter.between(start, end),
                                    pageToken = pageToken,
                                ),
                            )
                        HealthConnectMenstruationPage(
                            records = response.records.mapNotNull { it.toDomain() },
                            nextPageToken = response.pageToken,
                        )
                    }
                HealthConnectClientResult.Ok(collected)
            }.getOrElse { HealthConnectClientResult.Error(it.javaClass.simpleName) }
        }

        override suspend fun upsertMenstruationFlowExports(
            exports: List<MenstruationFlowExportWrite>,
        ): HealthConnectClientResult<Int> {
            if (exports.isEmpty()) return HealthConnectClientResult.Ok(0)
            val client = clientOrNull() ?: return HealthConnectClientResult.NotReady
            return runCatching {
                val records =
                    exports.map { write ->
                        MenstruationFlowRecord(
                            time =
                                MenstruationFlowMapper.localDateToInstantStartOfDay(
                                    write.localDate,
                                    exportZoneId,
                                ),
                            zoneOffset = exportZoneId.rules.getOffset(write.localDate.atStartOfDay()),
                            flow = write.flow.toSdkFlowInt(),
                            metadata = exportMetadata(write),
                        )
                    }
                client.insertRecords(records)
                HealthConnectClientResult.Ok(records.size)
            }.getOrElse { HealthConnectClientResult.Error(it.javaClass.simpleName) }
        }

        override suspend fun deleteMenstruationByClientRecordIds(
            clientRecordIds: List<String>,
        ): HealthConnectClientResult<Int> {
            if (clientRecordIds.isEmpty()) return HealthConnectClientResult.Ok(0)
            val client = clientOrNull() ?: return HealthConnectClientResult.NotReady
            return runCatching {
                client.deleteRecords(
                    MenstruationFlowRecord::class,
                    emptyList(),
                    clientRecordIds,
                )
                HealthConnectClientResult.Ok(clientRecordIds.size)
            }.getOrElse { HealthConnectClientResult.Error(it.javaClass.simpleName) }
        }

        private suspend fun clientOrNull(): HealthConnectClient? {
            if (availability() != HealthConnectAvailability.READY) return null
            return runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
        }

        private fun exportMetadata(write: MenstruationFlowExportWrite): Metadata =
            Metadata(
                id = "",
                dataOrigin = DataOrigin(context.packageName),
                lastModifiedTime = Instant.ofEpochMilli(write.updatedAtEpochMs),
                clientRecordId = MenstruationExportIds.clientRecordId(write.localDate),
                clientRecordVersion = write.updatedAtEpochMs,
                device =
                    Device(
                        manufacturer = "",
                        model = "",
                        type = Device.TYPE_UNKNOWN,
                    ),
                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
            )

        private fun MenstruationFlowRecord.toDomain(): HealthConnectMenstruationFlowDay? {
            val flow = flow.toDomainFlow() ?: return null
            val offset = zoneOffset ?: return null
            val localDate = MenstruationImportPolicy.localDateFromRecord(time, offset)
            return HealthConnectMenstruationFlowDay(
                localDate = localDate,
                flow = flow,
                sourceUpdatedAt = metadata.lastModifiedTime,
                dataOriginPackage = metadata.dataOrigin.packageName,
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
