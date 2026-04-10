package com.deysdeveloper.mutualfundapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user-created watchlist folder.
 */
@Entity(tableName = "watchlist_folders")
data class WatchlistFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String
)
