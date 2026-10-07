package dev.mahin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import dev.mahin.core.designsystem.MahinRadius
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.icon.MahinIcon
import dev.mahin.core.designsystem.mahinTextStyle

@Composable
fun MahinBottomNavigationBar(
    tabs: List<MahinNavTab>,
    selectedRoute: String?,
    modeAccent: Color,
    onTabSelected: (MahinNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barSurface =
        if (MaterialTheme.colorScheme.surface == MaterialTheme.colorScheme.background) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = barSurface,
        tonalElevation = MahinSpacing.xxs,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(horizontal = MahinSpacing.xs, vertical = MahinSpacing.xs),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                val label = stringResource(tab.labelRes)
                MahinBottomNavItem(
                    model =
                        MahinBottomNavItemModel(
                            tab = tab,
                            label = label,
                            selected = selected,
                            modeAccent = modeAccent,
                        ),
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class MahinBottomNavItemModel(
    val tab: MahinNavTab,
    val label: String,
    val selected: Boolean,
    val modeAccent: Color,
)

@Composable
private fun MahinBottomNavItem(
    model: MahinBottomNavItemModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tab = model.tab
    val label = model.label
    val selected = model.selected
    val modeAccent = model.modeAccent
    val pillColor =
        if (selected) {
            modeAccent.copy(alpha = 0.18f)
        } else {
            Color.Transparent
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier =
            modifier
                .defaultMinSize(minHeight = MahinSpacing.xxxl)
                .clip(RoundedCornerShape(MahinRadius.lg))
                .background(pillColor)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .semantics {
                    role = Role.Tab
                    this.selected = selected
                    contentDescription = label
                }.padding(vertical = MahinSpacing.xs, horizontal = MahinSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .defaultMinSize(minWidth = MahinSpacing.xxxl, minHeight = MahinSpacing.xxxl),
            contentAlignment = Alignment.Center,
        ) {
            MahinIcon(
                icon = tab.icon,
                tint = contentColor,
                decorative = true,
            )
        }
        Text(
            text = label,
            style = mahinTextStyle(MahinTypographyRole.Label),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
