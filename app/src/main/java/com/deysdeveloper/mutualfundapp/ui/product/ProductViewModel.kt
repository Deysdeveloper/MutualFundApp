package com.deysdeveloper.mutualfundapp.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.data.repository.WatchlistRepository
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import com.deysdeveloper.mutualfundapp.domain.model.NavEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class ProductUiState {
    data object Loading : ProductUiState()
    data class Success(
        val fundDetails: FundDetailsResponse,
        val chartData: List<NavEntry>,
        val isSaved: Boolean
    ) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val fundRepository: FundRepository,
    private val watchlistRepository: WatchlistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var currentSchemeCode: String = ""

    fun loadFund(schemeCode: String) {
        if (currentSchemeCode == schemeCode && _uiState.value is ProductUiState.Success) return
        currentSchemeCode = schemeCode

        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val details = fundRepository.getFundDetails(schemeCode)
                val isSaved = watchlistRepository.isFundSaved(schemeCode)
                val chartData = optimizeNavData(details.data)
                _uiState.value = ProductUiState.Success(
                    fundDetails = details,
                    chartData = chartData,
                    isSaved = isSaved
                )
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(
                    e.message ?: "Failed to load fund details."
                )
            }
        }
    }

    fun retry(schemeCode: String) {
        currentSchemeCode = ""
        loadFund(schemeCode)
    }

    fun toggleWatchlistSaved(schemeCode: String) {
        val current = _uiState.value as? ProductUiState.Success ?: return
        // Just flip the UI state; actual saving is done via WatchlistBottomSheet
        _uiState.value = current.copy(isSaved = !current.isSaved)
    }

    // ─── NAV data optimisation ────────────────────────────────────────────────

    /**
     * Takes last 365 entries (1 year of data) then downsamples every 10th entry
     * for smooth chart rendering without overloading the renderer.
     */
    private fun optimizeNavData(rawData: List<NavEntry>): List<NavEntry> {
        val lastYear = rawData.take(365)
        return lastYear.filterIndexed { index, _ -> index % 10 == 0 }
    }
}
