package com.deysdeveloper.mutualfundapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val results: List<Fund>) : SearchUiState()
    data object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

// ─── Constants ────────────────────────────────────────────────────────────────

private const val DEBOUNCE_MS = 300L
private const val MIN_QUERY_LENGTH = 2

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeQuery()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _query
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .filter { it.length >= MIN_QUERY_LENGTH }
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val results = fundRepository.searchFunds(query)
            _uiState.value = if (results.isEmpty()) {
                SearchUiState.Empty
            } else {
                SearchUiState.Success(results)
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(
                e.message ?: "Search failed. Please try again."
            )
        }
    }
}
