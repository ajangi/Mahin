package dev.mahin.android.pregnancy

data class PostPregnancyTransitionActions(
    val onResumeCycle: () -> Unit,
    val onResumeTtc: () -> Unit,
)
