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

    /**
     * Search for mutual funds matching [query].
     * Used by SearchViewModel — results are ephemeral and not cached.
     */
    suspend fun searchFunds(query: String): List<Fund> =
        apiService.searchFunds(query)

    /**
     * Fetch detailed NAV history for the fund identified by [schemeCode].
     */
    suspend fun getFundDetails(schemeCode: String): FundDetailsResponse =
        apiService.getFundDetails(schemeCode)

    /**
     * Offline-first fund list for the Explore screen.
     *
     * Behaviour:
     * 1. Starts collecting Room's reactive flow — any existing cache is emitted immediately.
     * 2. In parallel, fetches a fresh batch from the API, replaces the cached rows, and
     *    lets Room's Flow auto-notify the collector with updated data.
     * 3. If the network call fails the exception is silently swallowed; the cached
     *    emission already visible in the UI remains intact.
     */
    fun getFundsByCategory(category: String): Flow<List<Fund>> = channelFlow {
        // Step 1 — start streaming cached data from Room (emits immediately if cache exists)
        launch {
            cachedFundDao.getFundsByCategory(category)
                .map { list -> list.map { it.toFund() } }
                .collect { send(it) }
        }

        // Step 2 — refresh from network in the background
        try {
            val fresh = apiService.searchFunds(category)
            val entities = fresh.map { fund ->
                CachedFund(
                    category = category,
                    schemeCode = fund.schemeCode.toString(),
                    fundName = fund.schemeName,
                    nav = "" // NAV not returned by search endpoint
                )
            }
            // Replace stale cache then insert fresh batch
            cachedFundDao.deleteFundsByCategory(category)
            cachedFundDao.insertFunds(entities)
            // Room's reactive Flow will re-emit the updated list automatically
        } catch (_: Exception) {
            // Network unavailable — cached data already flowing; nothing extra to do
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun CachedFund.toFund(): Fund = Fund(
        schemeCode = schemeCode.toIntOrNull() ?: 0,
        schemeName = fundName
    )
}
