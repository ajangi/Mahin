package dev.mahin.core.designsystem.icon

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

/**
 * Renders a [MahinIconSpec] with theme tinting and localized (fa) content description.
 *
 * @param decorative When true, the icon is omitted from accessibility services (null contentDescription).
 */
@Composable
fun MahinIcon(
    icon: MahinIconSpec,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    decorative: Boolean = false,
) {
    Icon(
        painter = painterResource(icon.drawableRes),
        contentDescription =
            if (decorative) {
                null
            } else {
                stringResource(icon.contentDescriptionRes)
            },
        modifier = modifier,
        tint = tint,
    )
}
