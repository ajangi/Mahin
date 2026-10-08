package dev.mahin.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.mahin.core.designsystem.icon.MahinIconSpec

@Immutable
data class MahinNavTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: MahinIconSpec,
)
