package com.deysdeveloper.mutualfundapp.data.repository

import app.cash.turbine.test
import com.deysdeveloper.mutualfundapp.data.api.MfApiService
import com.deysdeveloper.mutualfundapp.data.local.dao.CachedFundDao
import com.deysdeveloper.mutualfundapp.data.local.entity.CachedFund
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FundRepositoryTest {

    private val apiService: MfApiService = mockk()
    private val cachedFundDao: CachedFundDao = mockk()
    private lateinit var repository: FundRepository

    @Before
    fun setUp() {
        repository = FundRepository(apiService, cachedFundDao)
    }

    @Test
    fun `getFundsByCategory should emit cached data and then refresh from network`() = runTest {
        // Given
        val category = "index"
        val cachedFunds = listOf(CachedFund(id = 1, category = category, schemeCode = "101", fundName = "Cached Fund", nav = "10.0"))
        val remoteFunds = listOf(Fund(101, "Remote Fund"))

        coEvery { cachedFundDao.getFundsByCategory(category) } returns flowOf(cachedFunds)
        coEvery { apiService.searchFunds(category) } returns remoteFunds
        coEvery { cachedFundDao.deleteFundsByCategory(category) } returns 1
        coEvery { cachedFundDao.insertFunds(any()) } returns listOf(1L)

        // When
        repository.getFundsByCategory(category).test {
            // Then
            val firstEmission = awaitItem()
            assertEquals(1, firstEmission.size)
            assertEquals("Cached Fund", firstEmission[0].schemeName)

            // Verify network interaction and cache update
            coVerify { apiService.searchFunds(category) }
            coVerify { cachedFundDao.deleteFundsByCategory(category) }
            coVerify { cachedFundDao.insertFunds(any()) }
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFundsByCategory network failure should still emit cached data`() = runTest {
        // Given
        val category = "index"
        val cachedFunds = listOf(CachedFund(id = 1, category = category, schemeCode = "101", fundName = "Cached Fund", nav = "10.0"))
        
        coEvery { cachedFundDao.getFundsByCategory(category) } returns flowOf(cachedFunds)
        coEvery { apiService.searchFunds(category) } throws Exception("Network timeout")

        // When & Then
        repository.getFundsByCategory(category).test {
            val emission = awaitItem()
            assertEquals("Cached Fund", emission[0].schemeName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFundsByCategory with empty cache and network failure should emit empty list`() = runTest {
        // Given
        val category = "index"
        coEvery { cachedFundDao.getFundsByCategory(category) } returns flowOf(emptyList())
        coEvery { apiService.searchFunds(category) } throws Exception("No Internet")

        // When & Then
        repository.getFundsByCategory(category).test {
            val emission = awaitItem()
            assertTrue(emission.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchFunds with empty results should return empty list`() = runTest {
        // Given
        coEvery { apiService.searchFunds("invalid") } returns emptyList()

        // When
        val result = repository.searchFunds("invalid")

        // Then
        assertTrue(result.isEmpty())
    }
}
