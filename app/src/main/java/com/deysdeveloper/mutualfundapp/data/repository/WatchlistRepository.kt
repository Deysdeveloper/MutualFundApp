package com.deysdeveloper.mutualfundapp.data.repository

import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund
import kotlinx.coroutines.flow.Flow
class WatchlistRepository(
    private val watchlistDao: WatchlistDao
) {

    // ─── Folder operations ─────────────────────────────────────────────────

    /**
     * Create a new watchlist folder with the given [name].
     * Returns the auto-generated row ID.
     */
    suspend fun addFolder(name: String): Long =
        watchlistDao.insertFolder(WatchlistFolder(name = name))

    /**
     * Observe all watchlist folders, sorted alphabetically.
     */
    fun getFolders(): Flow<List<WatchlistFolder>> =
        watchlistDao.getAllFolders()

    // ─── Fund operations ───────────────────────────────────────────────────

    /**
     * Add a fund identified by [schemeCode] into the folder with [folderId].
     * Returns the auto-generated row ID.
     */
    suspend fun addFundToFolder(schemeCode: String, folderId: Long): Long =
        watchlistDao.insertFund(
            WatchlistFund(schemeCode = schemeCode, folderId = folderId)
        )

    /**
     * Observe all funds belonging to the given [folderId].
     */
    fun getFundsInFolder(folderId: Long): Flow<List<WatchlistFund>> =
        watchlistDao.getFundsByFolder(folderId)

    /**
     * Check whether a fund with [schemeCode] already exists in any folder.
     */
    suspend fun isFundSaved(schemeCode: String): Boolean =
        watchlistDao.checkIfFundExists(schemeCode)
}
