package com.deysdeveloper.mutualfundapp.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class ExploreUiState {
    data object Loading : ExploreUiState()
    data class Success(val categories: Map<String, List<Fund>>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

// ─── Categories to fetch ──────────────────────────────────────────────────────

val EXPLORE_CATEGORIES: Map<String, String> = linkedMapOf(
    "Index Funds" to "index",
    "Bluechip Funds" to "bluechip",
    "Tax Saver (ELSS)" to "tax",
    "Large Cap Funds" to "large cap"
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            try {
                // Fetch all categories concurrently
                val results = EXPLORE_CATEGORIES.entries.map { (label, query) ->
                    label to async { fundRepository.searchFunds(query) }
                }.associate { (label, deferred) ->
                    label to deferred.await()
                }
                _uiState.value = ExploreUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = ExploreUiState.Error(
                    e.message ?: "Failed to load funds. Please try again."
                )
            }
        }
    }
}
