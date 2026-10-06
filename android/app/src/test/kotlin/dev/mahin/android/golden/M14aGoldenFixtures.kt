package dev.mahin.android.golden

import dev.mahin.android.cycle.CalendarUiState
import dev.mahin.android.cycle.DayMarkers
import dev.mahin.android.cycle.LogScreenActions
import dev.mahin.android.cycle.LogUiState
import dev.mahin.android.cycle.TodayUiState
import dev.mahin.android.cycle.TtcLogFormCallbacks
import dev.mahin.android.insights.CycleInsightsUiState
import dev.mahin.android.learn.LearnUiState
import dev.mahin.android.pregnancy.PregnancyHubActions
import dev.mahin.android.pregnancy.PregnancyHubContentState
import dev.mahin.core.content.ContentArticleSummaryDto
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import dev.mahin.domain.pregnancy.GestationalAge
import dev.mahin.domain.pregnancy.PregnancyDatingSnapshot
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import dev.mahin.domain.pregnancy.PregnancyTrimester
import dev.mahin.domain.subscription.CycleInsightsResult
import java.time.LocalDate

/** Synthetic, non-real health data for Roborazzi goldens (M14a). */
object M14aGoldenFixtures {
    const val SYNTHETIC_DISCLAIMER_FA = "داده‌های نمایشی آزمایشی — واقعی نیست."

    fun todayPopulated(): TodayUiState {
        val prediction =
            CyclePredictionResult(
                algorithmVersion = "cycle-prediction-v1",
                confidence = PredictionConfidence.MEDIUM,
                cycleDay = 28,
                nextPeriod =
                    DateRangeEstimate(
                        LocalDate.of(2025, 4, 1),
                        LocalDate.of(2025, 4, 5),
                    ),
                fertileWindow =
                    DateRangeEstimate(
                        LocalDate.of(2025, 3, 18),
                        LocalDate.of(2025, 3, 24),
                    ),
                estimatedOvulation = null,
                insufficientDataReason = null,
            )
        return TodayUiState(
            dashboard =
                CycleDashboard(
                    profile = null,
                    prediction = prediction,
                    todayLog = null,
                    onPeriodToday = true,
                    openPeriodStart = null,
                ),
        )
    }

    fun logPopulated(): LogUiState =
        LogUiState(
            selectedJalali = JalaliDate(1403, 6, 15),
        )

    fun logScreenActions(): LogScreenActions =
        LogScreenActions(
            onDateSelected = {},
            onToggleLoggingPeriod = {},
            onFlowLevelSelected = {},
            onToggleSymptom = {},
            onNoteChange = {},
            onSave = {},
            onTogglePregnancySymptom = {},
            onPregnancyWeightChange = {},
            onPregnancyBpSystolicChange = {},
            onPregnancyBpDiastolicChange = {},
            ttcCallbacks =
                TtcLogFormCallbacks(
                    onBbtChange = {},
                    onOvulationTestSelected = {},
                    onCervicalMucusSelected = {},
                    onIntercourseOptInChanged = {},
                    onIntercourseToggle = {},
                    onIntercourseProtectedSelected = {},
                    onPregnancyTestSelected = {},
                ),
        )

    fun historyPopulated(): List<PeriodRecordEntity> =
        listOf(
            PeriodRecordEntity(
                id = "fixture-p1",
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 5),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            ),
            PeriodRecordEntity(
                id = "fixture-p2",
                startDate = LocalDate.of(2025, 2, 1),
                endDate = LocalDate.of(2025, 2, 4),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            ),
        )

    fun calendarPopulated(): CalendarUiState {
        val selected = JalaliDate(year = 1403, month = 6, day = 15)
        val converter = PersianCivilDateConverter
        val markers = mutableMapOf<LocalDate, DayMarkers>()
        for (day in 1..31) {
            val jalali = JalaliDate(year = 1403, month = 6, day = day)
            val gregorian = converter.toGregorian(jalali)
            markers[gregorian] =
                DayMarkers(
                    loggedPeriod = day in 1..4,
                    predictedPeriod = day in 26..28,
                    fertileWindow = day in 11..17,
                )
        }
        return CalendarUiState(selectedJalali = selected, dayMarkers = markers)
    }

    fun cycleInsightsPopulated(): CycleInsightsUiState =
        CycleInsightsUiState(
            loading = false,
            insights =
                CycleInsightsResult(
                    algorithmVersion = "cycle-insights-v1",
                    cycleLengthDays = 26..32,
                    periodLengthDays = 4..6,
                    completedCycleCount = 3,
                    recentSymptomTimeline = listOf("خستگی (نمونه آزمایشی)"),
                    premium = null,
                    disclaimerFa = SYNTHETIC_DISCLAIMER_FA,
                ),
            hasPremium = false,
        )

    fun learnPopulated(): LearnUiState =
        LearnUiState(
            query = "",
            results =
                listOf(
                    ContentArticleSummaryDto(
                        id = "fixture-article-1",
                        slug = "synthetic-wellness",
                        locale = "fa-IR",
                        title = "[نمونه] مراقبت روزانه",
                        summary = "متن آزمایشی — محتوای واقعی نیست.",
                        contentType = "article",
                    ),
                    ContentArticleSummaryDto(
                        id = "fixture-article-2",
                        slug = "synthetic-cycle",
                        locale = "fa-IR",
                        title = "[نمونه] چرخه قاعدگی",
                        summary = "داده نمایشی برای تست طلایی.",
                        contentType = "article",
                    ),
                ),
        )

    fun pregnancyHubPopulated(): PregnancyHubContentState {
        val dating =
            PregnancyDatingSnapshot(
                lmpDate = LocalDate.of(2025, 1, 1),
                lmpBasedEdd = LocalDate.of(2025, 10, 8),
                clinicalEddDate = null,
                effectiveEddDate = LocalDate.of(2025, 10, 8),
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
            )
        val lmp = LocalDate.of(2025, 1, 1)
        val asOfDate = lmp.plusDays(149)
        val daysUntilEdd =
            dating.effectiveEddDate.toEpochDay() - asOfDate.toEpochDay()
        val status =
            PregnancyStatusSnapshot(
                dating = dating,
                gestationalAge = GestationalAge(weeks = 21, days = 2, totalDays = 149),
                trimester = PregnancyTrimester.SECOND,
                daysUntilEdd = daysUntilEdd,
                displayWeekNumber = 22,
            )
        return PregnancyHubContentState(
            isLoading = false,
            isPregnantMode = true,
            postTransition = false,
            status = status,
            kickSessionActive = false,
            kickCount = 0,
            kickElapsedSeconds = 0L,
            contractionSessionActive = false,
            contractionInProgress = false,
            contractionElapsedSeconds = 0L,
            appointments = emptyList(),
            newAppointmentTitle = "",
            newAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
            newAppointmentJalali = JalaliDate(1403, 10, 1),
            selectedOutcome = null,
            wantsSupportContent = false,
            suppressCelebratoryNotifications = false,
        )
    }

    fun pregnancyHubActions(): PregnancyHubActions =
        PregnancyHubActions(
            onNewAppointmentTitleChange = {},
            onNewAppointmentTypeSelected = {},
            onNewAppointmentDateSelected = {},
            onAddAppointment = {},
            onStartKickSession = {},
            onStopKickSession = {},
            onRecordKick = {},
            onStartContractionSession = {},
            onEndContractionSession = {},
            onToggleContraction = {},
            onOutcomeSelected = {},
            onSupportContentToggle = {},
            onSaveOutcome = {},
            onResumeCycle = {},
            onResumeTtc = {},
        )
}
