package com.deysdeveloper.mutualfundapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund

@Database(
    entities = [WatchlistFolder::class, WatchlistFund::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
