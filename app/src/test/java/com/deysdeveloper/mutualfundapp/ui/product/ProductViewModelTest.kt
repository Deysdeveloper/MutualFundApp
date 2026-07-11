package com.deysdeveloper.mutualfundapp.ui.product

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.data.repository.WatchlistRepository
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import com.deysdeveloper.mutualfundapp.domain.model.FundMeta
import com.deysdeveloper.mutualfundapp.domain.model.NavEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val fundRepository: FundRepository = mockk()
    private val watchlistRepository: WatchlistRepository = mockk()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductViewModel(fundRepository, watchlistRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFund should update uiState to Success when repository returns data`() = runTest {
        // Given
        val schemeCode = "12345"
        val mockDetails = FundDetailsResponse(
            meta = FundMeta("House", "Type", "Category", 12345, "Fund Name"),
            data = listOf(NavEntry("01-01-2023", "10.0")),
            status = "Success"
        )
        coEvery { fundRepository.getFundDetails(schemeCode) } returns mockDetails
        coEvery { watchlistRepository.isFundSaved(schemeCode) } returns true

        // When
        viewModel.loadFund(schemeCode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProductUiState.Success)
            val successState = state as ProductUiState.Success
            assertEquals(mockDetails, successState.fundDetails)
            assertTrue(successState.isSaved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadFund should handle empty NAV data`() = runTest {
        // Given
        val schemeCode = "12345"
        val mockDetails = FundDetailsResponse(
            meta = FundMeta("House", "Type", "Category", 12345, "Fund Name"),
            data = emptyList(),
            status = "Success"
        )
        coEvery { fundRepository.getFundDetails(schemeCode) } returns mockDetails
        coEvery { watchlistRepository.isFundSaved(schemeCode) } returns false

        // When
        viewModel.loadFund(schemeCode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProductUiState.Success)
            assertTrue((state as ProductUiState.Success).chartData.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadFund should not reload if same schemeCode is already Success`() = runTest {
        // Given
        val schemeCode = "12345"
        val mockDetails = FundDetailsResponse(
            meta = FundMeta("House", "Type", "Category", 12345, "Fund Name"),
            data = listOf(NavEntry("01-01-2023", "10.0")),
            status = "Success"
        )
        coEvery { fundRepository.getFundDetails(schemeCode) } returns mockDetails
        coEvery { watchlistRepository.isFundSaved(schemeCode) } returns false

        // When
        viewModel.loadFund(schemeCode)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadFund(schemeCode) // Second call
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { fundRepository.getFundDetails(schemeCode) }
    }
}
