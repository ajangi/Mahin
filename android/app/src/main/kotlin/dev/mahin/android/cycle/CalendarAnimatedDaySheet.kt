package dev.mahin.android.cycle

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.LocalReducedMotion
import java.time.LocalDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Suppress("LongParameterList")
@Composable
internal fun SharedTransitionScope.CalendarAnimatedDaySheet(
    open: Boolean,
    selectedJalali: JalaliDate,
    selectedGregorian: LocalDate,
    markers: DayMarkers?,
    logLines: List<String>,
    onDismiss: () -> Unit,
    onEditLog: (LocalDate) -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val sharedKey = calendarDaySharedContentKey(selectedGregorian)
    val enter =
        if (reducedMotion) {
            fadeIn()
        } else {
            slideInVertically { fullHeight -> fullHeight } + fadeIn()
        }
    val exit =
        if (reducedMotion) {
            fadeOut()
        } else {
            slideOutVertically { fullHeight -> fullHeight } + fadeOut()
        }
    val dismissLabel = stringResource(R.string.calendar_day_sheet_dismiss)
    val sheetPaneTitle = stringResource(R.string.calendar_day_sheet_pane_title)
    BackHandler(enabled = open, onBack = onDismiss)
    AnimatedVisibility(
        visible = open,
        enter = enter,
        exit = exit,
        modifier =
            Modifier
                .fillMaxSize()
                .testTag("calendar_day_sheet_overlay")
                .semantics {
                    isTraversalGroup = true
                    paneTitle = sheetPaneTitle
                },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .testTag("calendar_day_sheet_scrim")
                        .semantics {
                            contentDescription = dismissLabel
                            dismiss {
                                onDismiss()
                                true
                            }
                        }.clickable(onClick = onDismiss),
            )
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                CompositionLocalProvider(
                    LocalCalendarDaySheetTransitionScope provides this@AnimatedVisibility,
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(sharedKey),
                                    animatedVisibilityScope = this@AnimatedVisibility,
                                ).semantics { isTraversalGroup = true },
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 8.dp,
                    ) {
                        CalendarDaySheetContent(
                            jalali = selectedJalali,
                            gregorian = selectedGregorian,
                            markers = markers,
                            logLines = logLines,
                            onEditLog = { onEditLog(selectedGregorian) },
                            modifier = Modifier.testTag("calendar_day_sheet"),
                        )
                    }
                }
            }
        }
    }
}
