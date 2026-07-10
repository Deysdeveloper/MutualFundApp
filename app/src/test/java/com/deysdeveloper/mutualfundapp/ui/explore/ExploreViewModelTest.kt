package com.deysdeveloper.mutualfundapp.ui.explore

import app.cash.turbine.test
import com.deysdeveloper.mutualfundapp.data.repository.FundRepository
import com.deysdeveloper.mutualfundapp.domain.model.Fund
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ExploreViewModelTest {

    private val fundRepository: FundRepository = mockk()
    private lateinit var viewModel: ExploreViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load categories and update uiState to Success`() = runTest {
        // Given
        val mockFunds = listOf(Fund(1, "Test Fund"))
        coEvery { fundRepository.getFundsByCategory(any()) } returns flowOf(mockFunds)

        // When
        viewModel = ExploreViewModel(fundRepository)

        // Then
        viewModel.uiState.test {
            // Initially it might be Loading or Success depending on how fast the flow emits
            val initialState = awaitItem()
            
            if (initialState is ExploreUiState.Loading) {
                val successState = awaitItem()
                assertTrue(successState is ExploreUiState.Success)
                val categories = (successState as ExploreUiState.Success).categories
                assertTrue(categories.isNotEmpty())
                assertEquals(mockFunds, categories.values.first())
            } else {
                assertTrue(initialState is ExploreUiState.Success)
                val categories = (initialState as ExploreUiState.Success).categories
                assertTrue(categories.isNotEmpty())
                assertEquals(mockFunds, categories.values.first())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadCategories error should update uiState to Error if no data`() = runTest {
        // Given
        coEvery { fundRepository.getFundsByCategory(any()) } throws Exception("Network Error")

        // When
        viewModel = ExploreViewModel(fundRepository)
        viewModel.loadCategories()

        // Then
        viewModel.uiState.test {
            // Skip initial loading
            var state = awaitItem()
            while (state is ExploreUiState.Loading) {
                state = awaitItem()
            }
            
            assertTrue(state is ExploreUiState.Error)
            assertEquals("Network Error", (state as ExploreUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
