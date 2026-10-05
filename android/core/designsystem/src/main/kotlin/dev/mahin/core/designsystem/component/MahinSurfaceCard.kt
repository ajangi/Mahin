package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mahin.core.designsystem.MahinRadius
import dev.mahin.core.designsystem.MahinSpacing

@Composable
fun MahinSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(MahinRadius.md),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md), content = content)
    }
}
