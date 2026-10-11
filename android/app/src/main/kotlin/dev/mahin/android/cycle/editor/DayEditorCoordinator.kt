package dev.mahin.android.cycle.editor

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DayEditorSession(
    val date: LocalDate,
)

@Singleton
class DayEditorCoordinator
    @Inject
    constructor() {
        private val _session = MutableStateFlow<DayEditorSession?>(null)
        val session: StateFlow<DayEditorSession?> = _session.asStateFlow()

        fun open(date: LocalDate) {
            _session.value = DayEditorSession(date)
        }

        fun dismiss() {
            _session.update { null }
        }
    }
