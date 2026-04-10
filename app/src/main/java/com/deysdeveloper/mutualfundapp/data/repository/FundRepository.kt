package com.deysdeveloper.mutualfundapp.data.repository

import com.deysdeveloper.mutualfundapp.data.api.MfApiService
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
class FundRepository(
    private val apiService: MfApiService
) {

    /**
     * Search for mutual funds matching [query].
     * Returns an empty list on network failure instead of propagating the exception,
     * so callers can decide how to handle the error state.
     */
    suspend fun searchFunds(query: String): List<Fund> =
        apiService.searchFunds(query)

    /**
     * Fetch detailed NAV history for the fund identified by [schemeCode].
     */
    suspend fun getFundDetails(schemeCode: String): FundDetailsResponse =
        apiService.getFundDetails(schemeCode)
}
