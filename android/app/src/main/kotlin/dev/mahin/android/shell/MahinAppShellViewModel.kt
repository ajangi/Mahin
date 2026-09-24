package dev.mahin.android.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ShellNavigationState(
    val profileLoaded: Boolean = false,
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
)

@HiltViewModel
class MahinAppShellViewModel
    @Inject
    constructor(
        ttcRepository: TtcTrackingRepository,
    ) : ViewModel() {
        val navigationState: StateFlow<ShellNavigationState> =
            ttcRepository
                .observeProfile()
                .map { profile ->
                    ShellNavigationState(
                        profileLoaded = true,
                        reproductiveMode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ShellNavigationState(profileLoaded = false),
                )
    }
