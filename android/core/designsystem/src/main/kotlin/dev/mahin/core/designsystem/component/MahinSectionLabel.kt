package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

@Composable
fun MahinSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = mahinTextStyle(MahinTypographyRole.Label),
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = MahinSpacing.md, bottom = MahinSpacing.xs),
    )
}
