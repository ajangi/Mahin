package dev.mahin.android.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.content.ContentProgramDto
import dev.mahin.core.content.ProgramSessionDto
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinSurfaceCard

@Composable
fun ProgramDetailScreenContent(
    state: ProgramDetailUiState,
    onStartSession: (ProgramSessionDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val program = state.program
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md)
                .testTag("learn_program_detail_screen"),
    ) {
        if (program == null) {
            Text(text = stringResource(R.string.learn_loading))
            return
        }
        Text(text = program.title, style = MaterialTheme.typography.headlineSmall)
        program.summary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
        ProgramSafetyBlock(program = program)
        Text(
            text = stringResource(R.string.learn_program_completion_count, state.completionCount),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = MahinSpacing.sm),
        )
        LazyColumn {
            items(program.sessions.sortedBy { it.order }, key = { it.id }) { session ->
                MahinSurfaceCard(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.xs)) {
                    Column(modifier = Modifier.padding(MahinSpacing.md)) {
                        Text(text = session.title, style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { onStartSession(session) }) {
                            Text(stringResource(R.string.learn_program_start_session))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramSafetyBlock(program: ContentProgramDto) {
    val safetyText =
        listOfNotNull(
            program.essentialSafetyInformation,
            program.safetyNotice,
            program.contraindications,
        ).joinToString(separator = "\n\n")
    if (safetyText.isBlank()) return
    MahinSurfaceCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.sm)
                .testTag("learn_program_safety_block"),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.learn_program_safety_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(text = safetyText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
