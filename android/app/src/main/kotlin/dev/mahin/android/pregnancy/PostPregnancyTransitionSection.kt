package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinSettingsEntry
import dev.mahin.core.designsystem.component.MahinSettingsGroup

@Composable
fun PostPregnancyTransitionSection(
    actions: PostPregnancyTransitionActions,
    modifier: Modifier = Modifier,
    onOpenHistory: (() -> Unit)? = null,
    onOpenCycleCalendar: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("post_pregnancy_transition_section"),
    ) {
        Text(
            text = stringResource(R.string.pregnancy_post_transition_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_cycle),
            onClick = actions.onResumeCycle,
            modifier =
                Modifier
                    .testTag("post_pregnancy_resume_cycle")
                    .padding(top = MahinSpacing.md),
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_ttc),
            onClick = actions.onResumeTtc,
            modifier =
                Modifier
                    .testTag("post_pregnancy_resume_ttc")
                    .padding(top = MahinSpacing.sm),
        )
        PostPregnancySupportLinks(
            onOpenHistory = onOpenHistory,
            onOpenCycleCalendar = onOpenCycleCalendar,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
}

@Composable
private fun PostPregnancySupportLinks(
    onOpenHistory: (() -> Unit)?,
    onOpenCycleCalendar: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onOpenHistory == null && onOpenCycleCalendar == null) return
    val entries =
        buildList {
            onOpenCycleCalendar?.let {
                add(
                    MahinSettingsEntry(
                        label = stringResource(R.string.pregnancy_open_cycle_calendar),
                        onClick = it,
                    ),
                )
            }
            onOpenHistory?.let {
                add(
                    MahinSettingsEntry(
                        label = stringResource(R.string.pregnancy_open_past_cycles),
                        onClick = it,
                    ),
                )
            }
        }
    MahinSettingsGroup(
        title = stringResource(R.string.pregnancy_hub_links_heading),
        entries = entries,
        modifier = modifier.fillMaxWidth(),
    )
}
