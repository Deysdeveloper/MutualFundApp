package com.deysdeveloper.mutualfundapp.data.repository

import com.deysdeveloper.mutualfundapp.data.api.MfApiService
import com.deysdeveloper.mutualfundapp.data.local.dao.CachedFundDao
import com.deysdeveloper.mutualfundapp.data.local.entity.CachedFund
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FundRepository(
    private val apiService: MfApiService,
    private val cachedFundDao: CachedFundDao
) {

    suspend fun searchFunds(query: String): List<Fund> =
        apiService.searchFunds(query)

    suspend fun getFundDetails(schemeCode: String): FundDetailsResponse =
        apiService.getFundDetails(schemeCode)

    /**
     * Offline-first: emits cached data immediately, then refreshes from network in the background.
     * If the network call fails, the cached data already in the UI remains intact.
     */
    fun getFundsByCategory(category: String): Flow<List<Fund>> = channelFlow {
        launch {
            cachedFundDao.getFundsByCategory(category)
                .map { list -> list.map { it.toFund() } }
                .collect { send(it) }
        }

        try {
            val fresh = apiService.searchFunds(category)
            val entities = fresh.map { fund ->
                CachedFund(
                    category = category,
                    schemeCode = fund.schemeCode.toString(),
                    fundName = fund.schemeName,
                    nav = "" // NAV not returned by the search endpoint
                )
            }
            cachedFundDao.deleteFundsByCategory(category)
            cachedFundDao.insertFunds(entities)
        } catch (_: Exception) {
            // Network unavailable — cached data is still flowing
        }
    }

    private fun CachedFund.toFund(): Fund = Fund(
        schemeCode = schemeCode.toIntOrNull() ?: 0,
        schemeName = fundName
    )
}
