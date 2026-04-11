package com.deysdeveloper.mutualfundapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deysdeveloper.mutualfundapp.data.local.entity.CachedFund
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedFundDao {

    /** Returns a list of new row IDs for the inserted funds. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFunds(funds: List<CachedFund>): List<Long>

    /**
     * Removes stale entries for a category before inserting a fresh batch.
     * Returns the number of rows deleted.
     * Note: explicit Int return type required to avoid a Room + KSP code-gen
     * bug with `room.generateKotlin = true` where Unit (JVM "V") causes
     * an IllegalStateException during annotation processing.
     */
    @Query("DELETE FROM cached_funds WHERE category = :category")
    suspend fun deleteFundsByCategory(category: String): Int

    @Query("SELECT * FROM cached_funds WHERE category = :category")
    fun getFundsByCategory(category: String): Flow<List<CachedFund>>
}
