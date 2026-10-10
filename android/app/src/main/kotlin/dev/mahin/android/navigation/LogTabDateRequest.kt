package dev.mahin.android.navigation

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class LogTabDateRequest
    @Inject
    constructor() {
        private val _pendingDate = MutableStateFlow<LocalDate?>(null)
        val pendingDate: StateFlow<LocalDate?> = _pendingDate.asStateFlow()

        fun request(date: LocalDate) {
            _pendingDate.value = date
        }

        fun consume(): LocalDate? {
            val value = _pendingDate.value
            _pendingDate.value = null
            return value
        }
    }
