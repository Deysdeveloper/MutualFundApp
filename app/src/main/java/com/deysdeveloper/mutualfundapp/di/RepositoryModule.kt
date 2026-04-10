package com.deysdeveloper.mutualfundapp.di

import com.deysdeveloper.mutualfundapp.data.api.MfApiService
import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.data.repository.WatchlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFundRepository(apiService: MfApiService): FundRepository =
        FundRepository(apiService)

    @Provides
    @Singleton
    fun provideWatchlistRepository(watchlistDao: WatchlistDao): WatchlistRepository =
        WatchlistRepository(watchlistDao)
}
