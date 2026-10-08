package dev.mahin.android.cycle

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.pregnancy.PostPregnancyTransitionActions
import dev.mahin.android.pregnancy.PostPregnancyTransitionSection
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinCycleProgressRing
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.designsystem.icon.MahinIcon
import dev.mahin.core.designsystem.icon.MahinIcons
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.media.illustration.MahinIllustration
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CycleTodayHero
import dev.mahin.domain.cycle.TodaySnapshot
import dev.mahin.domain.pregnancy.PregnancyTodayHero
import java.time.LocalDate

@Composable
fun TodayScreen(
    onOpenHistory: () -> Unit = {},
    onOpenLearn: () -> Unit = {},
    actions: TodayScreenActions = TodayScreenActions(),
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreenContent(
        state = state,
        actions = actions,
        onOpenHistory = onOpenHistory,
        onOpenLearn = onOpenLearn,
        onWeekDaySelected = viewModel::onWeekDaySelected,
        onDismissDaySheet = viewModel::dismissDaySheet,
        onOpenConfidenceSheet = viewModel::openConfidenceSheet,
        onDismissConfidenceSheet = viewModel::dismissConfidenceSheet,
        postPregnancyActions =
            PostPregnancyTransitionActions(
                onResumeCycle = viewModel::resumeCycleTracking,
                onResumeTtc = viewModel::resumeTtc,
            ),
        modifier = modifier,
    )
}

@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun TodayScreenContent(
    state: TodayUiState,
    modifier: Modifier = Modifier,
    actions: TodayScreenActions = TodayScreenActions(),
    onOpenHistory: () -> Unit = {},
    onOpenLearn: () -> Unit = {},
    onWeekDaySelected: (LocalDate) -> Unit = {},
    onDismissDaySheet: () -> Unit = {},
    onOpenConfidenceSheet: () -> Unit = {},
    onDismissConfidenceSheet: () -> Unit = {},
    postPregnancyActions: PostPregnancyTransitionActions? = null,
) {
    val converter = PersianCivilDateConverter
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("today_screen_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            Text(
                text = stringResource(R.string.today_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.postPregnancyTransition && postPregnancyActions != null) {
            item {
                PostPregnancyTransitionSection(
                    actions = postPregnancyActions,
                    showLearnLink = state.postTransitionLearnLinkVisible,
                    onOpenHistory = onOpenHistory,
                    onOpenLearn = onOpenLearn,
                    modifier = Modifier.padding(top = MahinSpacing.md),
                )
            }
        }
        if (!state.postPregnancyTransition) {
            when (state.reproductiveMode) {
                ReproductiveMode.PREGNANT -> {
                    state.pregnancySnapshot?.hero?.let { hero ->
                        item { TodayPregnancyHero(hero = hero, onOpenCalendar = actions.onOpenCalendar) }
                        item {
                            TodayPregnancyQuickActions(
                                onOpenPlan = actions.onOpenPlan,
                                onOpenPregnancyTab = actions.onOpenPregnancyTab,
                            )
                        }
                    }
                }
                else -> {
                    when (val snapshot = state.todaySnapshot) {
                        TodaySnapshot.FirstDay, null -> {
                            item { TodayFirstDayCta(onLogFirstPeriod = actions.onOpenLogTab) }
                        }
                        is TodaySnapshot.Cycle -> {
                            item {
                                TodayCycleHero(
                                    hero = snapshot.hero,
                                    onConfidenceChipClick = onOpenConfidenceSheet,
                                )
                            }
                            item {
                                TodayWeekStrip(
                                    days = state.weekStrip,
                                    onDaySelected = onWeekDaySelected,
                                )
                            }
                            item {
                                TodayQuickLogRow(
                                    showTtcTest = state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE,
                                    onQuickLog = { actions.onOpenLogForDate(LocalDate.now()) },
                                )
                            }
                            state.loggedSummary?.takeIf { it.chips.isNotEmpty() }?.let { summary ->
                                item { TodayLoggedChips(summary = summary) }
                            }
                        }
                    }
                    if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                        item {
                            Text(
                                text = stringResource(R.string.today_ttc_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = MahinSpacing.sm),
                            )
                        }
                    }
                }
            }
            state.upcomingReminder?.let { reminder ->
                item {
                    TodayUpcomingCard(
                        title = reminder.descriptiveFa,
                        modifier = Modifier.padding(top = MahinSpacing.md),
                    )
                }
            }
        }
    }
    state.daySheetDate?.let { date ->
        CalendarDaySheet(
            open = true,
            selectedJalali = converter.toJalali(date),
            markers = state.daySheetMarkers,
            onDismiss = onDismissDaySheet,
            onEditLog = actions.onOpenLogForDate,
        )
    }
    if (state.showConfidenceSheet) {
        TodayConfidenceSheet(onDismiss = onDismissConfidenceSheet)
    }
}

@Composable
private fun TodayFirstDayCta(onLogFirstPeriod: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.lg),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        MahinIllustration(assetId = "editorial/empty-state/v1")
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        Button(onClick = onLogFirstPeriod, modifier = Modifier.testTag("today_first_period_cta")) {
            Text(text = stringResource(R.string.today_first_period_cta))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TodayCycleHero(
    hero: CycleTodayHero,
    onConfidenceChipClick: () -> Unit,
) {
    val cycleDay = hero.cycleDay
    val daysUntil = hero.daysUntilNextPeriodEarliest
    val statusLine =
        when {
            hero.isOverdue -> stringResource(R.string.today_status_overdue)
            daysUntil != null && daysUntil >= 0 ->
                stringResource(
                    R.string.today_status_days_until_period,
                    PersianDigits.format(daysUntil),
                )
            else -> stringResource(R.string.today_status_tracking)
        }
    val a11y =
        cycleDay?.let {
            stringResource(
                R.string.today_ring_content_description,
                PersianDigits.format(it),
                statusLine,
            )
        }
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            MahinCycleProgressRing(
                arcs = TodayCycleHeroMapper.ringArcs(hero),
                progressFraction = TodayCycleHeroMapper.progressFraction(hero),
                trackColor = TodayCycleHeroMapper.trackColor(),
                progressColor = MaterialTheme.colorScheme.primary,
                todayMarkerFraction = TodayCycleHeroMapper.todayMarkerFraction(hero),
                todayMarkerColor = MaterialTheme.colorScheme.primary,
                contentDescription = a11y,
            ) {
                cycleDay?.let {
                    Text(
                        text = stringResource(R.string.today_cycle_day_numeric, PersianDigits.format(it)),
                        style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                        modifier = Modifier.semantics { invisibleToUser() },
                    )
                }
            }
            Text(
                text = statusLine,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
            if (hero.isInFertileWindow) {
                Text(
                    text = stringResource(R.string.today_fertile_not_contraception),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .padding(top = MahinSpacing.xs)
                            .testTag("fertile_not_contraception_copy"),
                )
            }
            if (hero.showConfidenceChip) {
                FilterChip(
                    selected = false,
                    onClick = onConfidenceChipClick,
                    label = { Text(text = stringResource(R.string.today_confidence_chip)) },
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TodayPregnancyHero(
    hero: PregnancyTodayHero,
    onOpenCalendar: () -> Unit,
) {
    val gaLabel =
        stringResource(
            R.string.today_pregnancy_ga,
            PersianDigits.format(hero.gestationalWeeks),
            PersianDigits.format(hero.gestationalDays),
        )
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        Column {
            val weekProgress = (hero.gestationalWeeks * 7 + hero.gestationalDays).toFloat() / hero.totalGestationalDays
            MahinCycleProgressRing(
                arcs = TodayPregnancyRingMapper.weekArcs(hero),
                progressFraction = weekProgress.coerceIn(0f, 1f),
                trackColor = TodayCycleHeroMapper.trackColor(),
                progressColor = MaterialTheme.colorScheme.secondary,
                todayMarkerFraction = weekProgress.coerceIn(0f, 1f),
                todayMarkerColor = MaterialTheme.colorScheme.secondary,
                contentDescription = gaLabel,
            ) {
                Text(
                    text = gaLabel,
                    style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                    modifier = Modifier.semantics { invisibleToUser() },
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_edd_countdown,
                        PersianDigits.format(hero.daysUntilEdd),
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onOpenCalendar, modifier = Modifier.padding(top = MahinSpacing.sm)) {
                Text(text = stringResource(R.string.pregnancy_open_cycle_calendar))
            }
        }
    }
}

@Composable
private fun TodayWeekStrip(
    days: List<TodayWeekDay>,
    onDaySelected: (LocalDate) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("today_week_strip"),
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
    ) {
        days.forEach { day ->
            FilterChip(
                selected = day.isSelected,
                onClick = { onDaySelected(day.date) },
                label = { Text(text = PersianDigits.format(day.jalali.day)) },
            )
        }
    }
}

@Composable
private fun TodayQuickLogRow(
    showTtcTest: Boolean,
    onQuickLog: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("today_quick_log_row"),
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        QuickLogChip(
            icon = MahinIcons.Flow.medium,
            label = stringResource(R.string.today_quick_period),
            onClick = onQuickLog,
        )
        QuickLogChip(
            icon = MahinIcons.Symptom.fatigue,
            label = stringResource(R.string.today_quick_symptoms),
            onClick = onQuickLog,
        )
        QuickLogChip(
            icon = MahinIcons.Mood.calm,
            label = stringResource(R.string.today_quick_mood),
            onClick = onQuickLog,
        )
        if (showTtcTest) {
            QuickLogChip(
                icon = MahinIcons.Tests.opk,
                label = stringResource(R.string.today_quick_test),
                onClick = onQuickLog,
            )
        }
    }
}

@Composable
private fun QuickLogChip(
    icon: dev.mahin.core.designsystem.icon.MahinIconSpec,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs)) {
                MahinIcon(icon = icon, decorative = true)
                Text(text = label)
            }
        },
    )
}

@Composable
private fun TodayLoggedChips(summary: TodayLoggedSummary) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("today_logged_summary"),
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        summary.chips.forEach { chip ->
            FilterChip(
                selected = true,
                onClick = {},
                enabled = false,
                label = {
                    Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xs)) {
                        MahinIcon(icon = chip.icon, decorative = true)
                        Text(text = chip.label)
                    }
                },
            )
        }
    }
}

@Composable
private fun TodayUpcomingCard(
    title: String,
    modifier: Modifier = Modifier,
) {
    MahinSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.today_upcoming_title),
            style = mahinTextStyle(MahinTypographyRole.Label),
        )
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TodayPregnancyQuickActions(
    onOpenPlan: () -> Unit,
    onOpenPregnancyTab: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        Button(onClick = onOpenPlan) { Text(text = stringResource(R.string.today_open_plan)) }
        Button(onClick = onOpenPregnancyTab) { Text(text = stringResource(R.string.today_open_pregnancy_tools)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayConfidenceSheet(onDismiss: () -> Unit) {
    val skip = LocalReducedMotion.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skip)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(MahinSpacing.lg)) {
            Text(
                text = stringResource(R.string.today_confidence_sheet_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.today_confidence_sheet_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
            Text(
                text = stringResource(R.string.today_fertile_not_contraception),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}
