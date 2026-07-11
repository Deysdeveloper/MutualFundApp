package com.deysdeveloper.mutualfundapp.data.repository

import com.deysdeveloper.mutualfundapp.data.local.dao.WatchlistDao
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WatchlistRepositoryTest {

    private val watchlistDao: WatchlistDao = mockk()
    private lateinit var repository: WatchlistRepository

    @Before
    fun setUp() {
        repository = WatchlistRepository(watchlistDao)
    }

    @Test
    fun `isFundSaved should return true when fund exists`() = runTest {
        coEvery { watchlistDao.getFundIdByScheme("101") } returns 1L
        val result = repository.isFundSaved("101")
        assertTrue(result)
    }

    @Test
    fun `isFundSaved should return false when fund does not exist`() = runTest {
        coEvery { watchlistDao.getFundIdByScheme("999") } returns null
        val result = repository.isFundSaved("999")
        assertTrue(!result)
    }

    @Test
    fun `addFolder should call dao`() = runTest {
        coEvery { watchlistDao.insertFolder(any()) } returns 1L
        val result = repository.addFolder("My Folder")
        assertEquals(1L, result)
        coVerify { watchlistDao.insertFolder(match { it.name == "My Folder" }) }
    }
}
