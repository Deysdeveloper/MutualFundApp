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

    // ─── Delete operations ─────────────────────────────────────────────────

    /**
     * Deletes the folder. Cascade rule on [WatchlistFund] removes all its funds automatically.
     * Returns Int (not Unit) to avoid the Room + KSP `room.generateKotlin` void-signature bug.
     */
    @Query("DELETE FROM watchlist_folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long): Int

    /**
     * Removes a single fund entry from a folder by its primary key.
     */
    @Query("DELETE FROM watchlist_funds WHERE id = :fundId")
    suspend fun deleteFund(fundId: Long): Int
}
