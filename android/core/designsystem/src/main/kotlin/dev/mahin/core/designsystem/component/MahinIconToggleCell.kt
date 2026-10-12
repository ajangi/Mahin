package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinSpacing

@Composable
fun MahinIconToggleCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .padding(MahinSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text = label) },
            leadingIcon = icon,
        )
    }
}
