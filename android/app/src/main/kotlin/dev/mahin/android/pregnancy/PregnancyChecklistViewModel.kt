package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.pregnancy.PregnancyChecklistRepository
import dev.mahin.core.datetime.PersianDigits
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PregnancyChecklistViewModel
    @Inject
    constructor(
        private val checklistRepository: PregnancyChecklistRepository,
        private val contentRepository: ContentRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyChecklistUiState())
        val uiState: StateFlow<PregnancyChecklistUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val template = contentRepository.checklistTemplate()
                template?.items?.let { items ->
                    checklistRepository.seedFromTemplateIfEmpty(
                        items.map {
                            PregnancyChecklistRepository.TemplateItem(
                                templateId = it.id,
                                section = it.section,
                                label = it.label,
                                order = it.order,
                            )
                        },
                    )
                }
            }
            viewModelScope.launch {
                checklistRepository.observeHospitalBag().collect { entities ->
                    val ui =
                        entities.map {
                            PregnancyChecklistItemUi(
                                id = it.id,
                                section = it.section,
                                label = it.label,
                                checked = it.checked,
                            )
                        }
                    val checked = ui.count { it.checked }
                    val progress =
                        if (ui.isEmpty()) {
                            ""
                        } else {
                            val pct = (checked * 100) / ui.size
                            PersianDigits.format(pct) + "٪"
                        }
                    _uiState.update {
                        it.copy(isLoading = false, items = ui, progressLabel = progress)
                    }
                }
            }
        }

        fun toggleItem(
            id: String,
            checked: Boolean,
        ) {
            viewModelScope.launch {
                checklistRepository.setChecked(id, checked)
            }
        }
    }
