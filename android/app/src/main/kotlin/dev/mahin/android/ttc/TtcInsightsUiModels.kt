package dev.mahin.android.ttc

import androidx.compose.ui.Modifier
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.domain.fertility.FertilityInsightResult

data class TtcInsightsContentState(
    val isLoading: Boolean,
    val isTtcMode: Boolean,
    val intercourseLoggingEnabled: Boolean,
    val insight: FertilityInsightResult?,
    val timeline: List<TtcDayLogEntity>,
    val bbtPoints: List<BbtChartPoint>,
    val modifier: Modifier = Modifier,
)

data class TtcIntercourseSectionState(
    val enabled: Boolean,
    val intercourseLogged: Boolean,
    val intercourseProtected: Boolean?,
)

data class TtcIntercourseSectionCallbacks(
    val onOptInChanged: (Boolean) -> Unit,
    val onIntercourseToggle: () -> Unit,
    val onIntercourseProtectedSelected: (Boolean?) -> Unit,
)
