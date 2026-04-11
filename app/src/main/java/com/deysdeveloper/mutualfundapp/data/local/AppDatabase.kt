package com.deysdeveloper.mutualfundapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deysdeveloper.mutualfundapp.data.local.dao.CachedFundDao
import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import com.deysdeveloper.mutualfundapp.data.local.entity.CachedFund
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund

@Database(
    entities = [WatchlistFolder::class, WatchlistFund::class, CachedFund::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun cachedFundDao(): CachedFundDao
}
