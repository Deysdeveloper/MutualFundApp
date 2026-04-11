package com.deysdeveloper.mutualfundapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    // ─── Folder operations ─────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: WatchlistFolder): Long

    @Query("SELECT * FROM watchlist_folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<WatchlistFolder>>

    // ─── Fund operations ───────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: WatchlistFund): Long

    @Query("SELECT * FROM watchlist_funds WHERE folderId = :folderId")
    fun getFundsByFolder(folderId: Long): Flow<List<WatchlistFund>>

    @Query("SELECT id FROM watchlist_funds WHERE schemeCode = :schemeCode LIMIT 1")
    suspend fun getFundIdByScheme(schemeCode: String): Long?
}
