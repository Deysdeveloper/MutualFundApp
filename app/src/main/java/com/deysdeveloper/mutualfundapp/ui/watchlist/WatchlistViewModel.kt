package com.deysdeveloper.mutualfundapp.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund
import com.deysdeveloper.mutualfundapp.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class WatchlistUiState {
    data object Loading : WatchlistUiState()
    data class Success(val folders: List<WatchlistFolder>) : WatchlistUiState()
    // No error needed for local DB; an empty list is a valid state
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    /** All folders as a hot flow backed by Room. */
    val folders: StateFlow<List<WatchlistFolder>> = watchlistRepository
        .getFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Funds within a specific folder — collected on-demand by FolderDetailScreen. */
    fun getFundsInFolder(folderId: Long): StateFlow<List<WatchlistFund>> =
        watchlistRepository.getFundsInFolder(folderId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** Creates a new watchlist folder. */
    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            watchlistRepository.addFolder(name.trim())
        }
    }

    /**
     * Saves [schemeCode] into each folder in [folderIds].
     * Skips folders that already contain the fund (Room REPLACE strategy handles duplicates).
     */
    fun saveFundToFolders(schemeCode: String, folderIds: List<Long>) {
        viewModelScope.launch {
            folderIds.forEach { folderId ->
                watchlistRepository.addFundToFolder(schemeCode, folderId)
            }
        }
    }
}
