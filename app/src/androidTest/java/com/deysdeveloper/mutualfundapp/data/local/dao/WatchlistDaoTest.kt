package com.deysdeveloper.mutualfundapp.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deysdeveloper.mutualfundapp.data.local.AppDatabase
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFolder
import com.deysdeveloper.mutualfundapp.data.local.entity.WatchlistFund
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var watchlistDao: WatchlistDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        watchlistDao = database.watchlistDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetFolders() = runBlocking {
        val folder = WatchlistFolder(name = "My Favorites")
        watchlistDao.insertFolder(folder)

        val folders = watchlistDao.getAllFolders().first()
        assertEquals(1, folders.size)
        assertEquals("My Favorites", folders[0].name)
    }

    @Test
    fun insertAndGetFunds() = runBlocking {
        val folderId = watchlistDao.insertFolder(WatchlistFolder(name = "My Favorites"))
        
        val fund = WatchlistFund(schemeCode = "12345", folderId = folderId)
        watchlistDao.insertFund(fund)

        val funds = watchlistDao.getFundsByFolder(folderId).first()
        assertEquals(1, funds.size)
        assertEquals("12345", funds[0].schemeCode)
    }

    @Test
    fun deleteFolderAlsoDeletesFunds() = runBlocking {
        val folderId = watchlistDao.insertFolder(WatchlistFolder(name = "To Delete"))
        watchlistDao.insertFund(WatchlistFund(schemeCode = "12345", folderId = folderId))

        watchlistDao.deleteFolder(folderId)

        val folders = watchlistDao.getAllFolders().first()
        assertTrue(folders.isEmpty())

        val funds = watchlistDao.getFundsByFolder(folderId).first()
        assertTrue(funds.isEmpty())
    }
}
