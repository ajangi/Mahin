package dev.mahin.core.healthconnect

import com.google.common.truth.Truth.assertThat
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlow
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HealthConnectMenstruationFlowPagerTest {
    @Test
    fun followsPageTokensUntilEmpty() =
        runTest {
            var calls = 0
            val all =
                HealthConnectMenstruationFlowPager.readAllPages { token ->
                    calls++
                    when (token) {
                        null ->
                            HealthConnectMenstruationPage(
                                records =
                                    listOf(
                                        day(LocalDate.of(2024, 1, 1)),
                                    ),
                                nextPageToken = "page-2",
                            )
                        "page-2" ->
                            HealthConnectMenstruationPage(
                                records = listOf(day(LocalDate.of(2024, 1, 2))),
                                nextPageToken = null,
                            )
                        else -> error("unexpected token")
                    }
                }
            assertThat(calls).isEqualTo(2)
            assertThat(all).hasSize(2)
        }

    private fun day(date: LocalDate): HealthConnectMenstruationFlowDay =
        HealthConnectMenstruationFlowDay(
            localDate = date,
            flow = HealthConnectMenstruationFlow.LIGHT,
            sourceUpdatedAt = Instant.EPOCH,
        )
}
