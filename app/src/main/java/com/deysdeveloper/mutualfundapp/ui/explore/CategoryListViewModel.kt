package com.deysdeveloper.mutualfundapp.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class CategoryListUiState {
    data object Loading : CategoryListUiState()
    data class Success(val funds: List<Fund>) : CategoryListUiState()
    data object Empty : CategoryListUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryListUiState>(CategoryListUiState.Loading)
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    /**
     * Called from the screen via LaunchedEffect — mirrors the pattern used in
     * ProductViewModel.loadFund so that the query is passed explicitly rather
     * than through SavedStateHandle (Navigation 3 doesn't auto-populate it).
     */
    fun loadCategory(query: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.value = CategoryListUiState.Loading
            // Offline-first: Room cache emits first, then network refresh auto re-emits
            fundRepository.getFundsByCategory(query).collect { funds ->
                _uiState.value = if (funds.isEmpty()) {
                    CategoryListUiState.Empty
                } else {
                    CategoryListUiState.Success(funds)
                }
            }
        }
    }
}
