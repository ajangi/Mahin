package dev.mahin.android.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsScreenCallbacks(
    val onNavigateUp: () -> Unit,
    val onModeSelected: (dev.mahin.core.model.ReproductiveMode) -> Unit,
    val onOpenNotifications: () -> Unit,
    val onOpenPrivacy: () -> Unit,
    val onOpenHealthConnect: () -> Unit,
    val onOpenAssistant: () -> Unit,
    val onOpenDataExport: () -> Unit,
    val onOpenHistory: () -> Unit,
    val onResumeCycle: () -> Unit,
    val onResumeTtc: () -> Unit,
)
