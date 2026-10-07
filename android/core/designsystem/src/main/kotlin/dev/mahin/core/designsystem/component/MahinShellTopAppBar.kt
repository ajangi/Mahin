package dev.mahin.core.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.icon.MahinIcon
import dev.mahin.core.designsystem.icon.MahinIcons
import dev.mahin.core.designsystem.mahinTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahinShellTopAppBar(
    title: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    showSettingsAction: Boolean = true,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = mahinTextStyle(MahinTypographyRole.Title),
                modifier = Modifier.semantics { heading() },
            )
        },
        actions = {
            if (showSettingsAction) {
                IconButton(onClick = onOpenSettings) {
                    MahinIcon(icon = MahinIcons.Nav.settings)
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahinShellSecondaryTopAppBar(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = mahinTextStyle(MahinTypographyRole.Title),
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                MahinIcon(icon = MahinIcons.Action.back)
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    )
}
