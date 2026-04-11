package com.deysdeveloper.mutualfundapp.data.api

import com.deysdeveloper.mutualfundapp.domain.model.Fund
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the mfapi.in API.
 * Base URL: https://api.mfapi.in/
 */
interface MfApiService {

    /**
     * Search for mutual funds by name or keyword.
     * Endpoint: GET /mf/search?q={query}
     */
    @GET("mf/search")
    suspend fun searchFunds(
        @Query("q") query: String
    ): List<Fund>

    /**
     * Fetch full details + NAV history for a specific scheme.
     * Endpoint: GET /mf/{schemeCode}
     */
    @GET("mf/{schemeCode}")
    suspend fun getFundDetails(
        @Path("schemeCode") schemeCode: String
    ): FundDetailsResponse

    @POST("mf/{schemeCode}/watchlist")
    suspend fun addToWatchlist(
        @Path("schemeCode") schemeCode: String
    )
}
