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

@HiltViewModel
class MahinAppShellViewModel
    @Inject
    constructor(
        ttcRepository: TtcTrackingRepository,
    ) : ViewModel() {
        val reproductiveMode: StateFlow<ReproductiveMode> =
            ttcRepository
                .observeProfile()
                .map { profile -> profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ReproductiveMode.CYCLE_TRACKING,
                )
    }
