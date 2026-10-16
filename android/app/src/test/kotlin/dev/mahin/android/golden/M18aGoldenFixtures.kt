package dev.mahin.android.golden

import dev.mahin.android.pregnancy.PregnancyChecklistItemUi
import dev.mahin.android.pregnancy.PregnancyChecklistUiState
import dev.mahin.android.pregnancy.PregnancyPlanTimelineItemUi
import dev.mahin.android.pregnancy.PregnancyWeekByWeekUiState
import dev.mahin.android.pregnancy.PregnancyWeekPageState
import java.time.LocalDate

object M18aGoldenFixtures {
    fun weekByWeek(): PregnancyWeekByWeekUiState =
        PregnancyWeekByWeekUiState(
            isLoading = false,
            currentWeek = 18,
            pages =
                listOf(
                    PregnancyWeekPageState(
                        weekNumber = 18,
                        title = "هفتهٔ ۱۸",
                        fetalSummary = "نمونه — غیرپزشکی: رشد جنین",
                        maternalSummary = "نمونه — غیرپزشکی: تغییرات بدن",
                        sizeComparisonLabel = "نمونه — اندازهٔ تقریبی",
                        illustrationAssetId = "pregnancy/fetal-development/week-18/v1",
                    ),
                ),
        )

    fun planTimeline(): List<PregnancyPlanTimelineItemUi> =
        listOf(
            PregnancyPlanTimelineItemUi(
                title = "آزمایش خون",
                subtitle = "هفتهٔ ۸–۱۰",
                isCarePlan = true,
            ),
            PregnancyPlanTimelineItemUi(
                title = "ویزیت ماهانه",
                subtitle = "۱۴۰۴/۰۴/۰۱",
                isCarePlan = false,
            ),
        )

    fun checklist(): PregnancyChecklistUiState =
        PregnancyChecklistUiState(
            isLoading = false,
            items =
                listOf(
                    PregnancyChecklistItemUi("1", "مدارک", "کارت ملی", false),
                    PregnancyChecklistItemUi("2", "لباس", "رویه", true),
                ),
            progressLabel = "۵۰٪",
        )

    fun toolsReferenceDate(): LocalDate = LocalDate.of(2025, 7, 1)
}
