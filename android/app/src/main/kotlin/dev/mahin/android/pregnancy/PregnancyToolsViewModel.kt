package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PregnancyToolsViewModel
    @Inject
    constructor(
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        fun referenceDate(): LocalDate = localDateClock.today()
    }
