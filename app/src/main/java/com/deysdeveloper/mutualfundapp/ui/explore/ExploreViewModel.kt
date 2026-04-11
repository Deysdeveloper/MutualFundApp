package com.deysdeveloper.mutualfundapp.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    /**
     * Holds the live category data as it streams in.
     * Updated incrementally — each category updates the map independently so
     * the UI can show cached content as soon as the first category emits.
     */
    private val _categoryData = MutableStateFlow<Map<String, List<Fund>>>(emptyMap())

    init {
        observeCategoryData()
        loadCategories()
    }

    /**
     * Observes the accumulated category map and pushes Success state whenever
     * at least one category has data.
     */
    private fun observeCategoryData() {
        viewModelScope.launch {
            _categoryData.collect { map ->
                if (map.isNotEmpty()) {
                    _uiState.value = ExploreUiState.Success(map)
                }
            }
        }
    }

    /**
     * Launches a separate coroutine per category that collects the offline-first
     * Flow from the repository. Each emission (cached or fresh) updates the shared
     * category map, which triggers a UI recomposition.
     */
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            _categoryData.value = emptyMap()

            EXPLORE_CATEGORIES.entries.forEach { (label, query) ->
                launch {
                    try {
                        fundRepository.getFundsByCategory(query).collect { funds ->
                            _categoryData.update { current ->
                                current + (label to funds)
                            }
                        }
                    } catch (e: Exception) {
                        // If a category completely fails and has no cache,
                        // surface an error only when no data has arrived at all.
                        if (_categoryData.value.isEmpty()) {
                            _uiState.value = ExploreUiState.Error(
                                e.message ?: "Failed to load funds. Please try again."
                            )
                        }
                    }
                }
            }
        }
    }
}
