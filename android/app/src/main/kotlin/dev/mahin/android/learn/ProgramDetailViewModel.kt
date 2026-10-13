package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.WellnessContentLocalStore
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProgramDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val contentRepository: ContentRepository,
        private val localStore: WellnessContentLocalStore,
    ) : ViewModel() {
        private val programId: String = savedStateHandle.get<String>("programId") ?: ""
        private val _uiState = MutableStateFlow(ProgramDetailUiState())
        val uiState: StateFlow<ProgramDetailUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val program = contentRepository.program(programId)
                val count = localStore.programCompletionCount(programId)
                _uiState.update { it.copy(program = program, completionCount = count, loading = false) }
            }
        }
    }
