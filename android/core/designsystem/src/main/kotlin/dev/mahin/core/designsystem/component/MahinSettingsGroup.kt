package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

@Composable
fun MahinSettingsGroup(
    title: String,
    entries: List<MahinSettingsEntry>,
    modifier: Modifier = Modifier,
) {
    MahinSurfaceCard(modifier = modifier) {
        Text(
            text = title,
            style = mahinTextStyle(MahinTypographyRole.Label),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.fillMaxWidth().padding(top = MahinSpacing.xs)) {
            entries.forEachIndexed { index, entry ->
                MahinTextButton(
                    text = entry.label,
                    onClick = entry.onClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index < entries.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
