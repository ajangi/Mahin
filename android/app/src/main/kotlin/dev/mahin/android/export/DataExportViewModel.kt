package dev.mahin.android.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.billing.HealthExportResult
import dev.mahin.core.billing.HealthExportService
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataExportUiState(
    val loading: Boolean = false,
    val exportJson: String? = null,
    val locked: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DataExportViewModel
    @Inject
    constructor(
        private val healthExportService: HealthExportService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DataExportUiState())
        val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()

        fun generateExport() {
            viewModelScope.launch {
                _uiState.value = DataExportUiState(loading = true)
                when (val result = healthExportService.buildJsonExport()) {
                    is HealthExportResult.Locked ->
                        _uiState.value =
                            DataExportUiState(locked = true)
                    is HealthExportResult.Success ->
                        _uiState.value =
                            DataExportUiState(exportJson = result.json)
                }
            }
        }

        fun clear() {
            _uiState.value = DataExportUiState()
        }
    }
