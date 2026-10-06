package dev.mahin.core.designsystem.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconCatalogueGrid(
    icons: List<MahinIconSpec>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
        maxItemsInEachRow = 4,
    ) {
        icons.forEach { icon ->
            Column(
                modifier = Modifier.width(88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
            ) {
                MahinIcon(
                    icon = icon,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = icon.semanticId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

/** Single-family sheet for Roborazzi goldens and the debug catalogue. */
@Composable
fun IconCatalogueFamilyGoldenSheet(
    familyPrefix: String,
    title: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val icons = MahinIcons.all.filter { it.semanticId.startsWith("$familyPrefix/") }
    MahinTheme(darkTheme = darkTheme) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(MahinSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
        ) {
            Text(
                text = title,
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconCatalogueGrid(icons = icons)
        }
    }
}

@Composable
fun IconCatalogueRtlMirrorCheck(
    darkTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    MahinTheme(darkTheme = darkTheme) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(MahinSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MahinSpacing.lg),
        ) {
            Text(
                text = "آینهٔ RTL — آیکون‌های جهت‌دار",
                style = mahinTextStyle(MahinTypographyRole.Title),
            )
            listOf(MahinIcons.Action.back, MahinIcons.Action.share, MahinIcons.Action.undo).forEach { icon ->
                Column(verticalArrangement = Arrangement.spacedBy(MahinSpacing.xs)) {
                    MahinIcon(icon = icon, modifier = Modifier.size(48.dp))
                    Text(text = icon.semanticId, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
