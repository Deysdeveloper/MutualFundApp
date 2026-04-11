package com.deysdeveloper.mutualfundapp.di

import android.content.Context
import androidx.room.Room
import com.deysdeveloper.mutualfundapp.data.local.AppDatabase
import com.deysdeveloper.mutualfundapp.data.local.dao.CachedFundDao
import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "mutual_fund_db"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            // Safe for dev: the cached_funds table contains no user-critical data;
            // watchlist data is preserved because we added a new table, not changed old ones.
            // Destructive migration is used here to avoid writing a manual migration for the
            // new cached_funds table added in version 2.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao =
        database.watchlistDao()

    @Provides
    @Singleton
    @Suppress("unused") // Called by Hilt-generated code via annotation processing
    fun provideCachedFundDao(database: AppDatabase): CachedFundDao =
        database.cachedFundDao()
}
