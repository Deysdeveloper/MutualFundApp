package com.deysdeveloper.mutualfundapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a mutual fund saved inside a watchlist folder.
 * Has a foreign-key relationship to [WatchlistFolder].
 */
@Entity(
    tableName = "watchlist_funds",
    foreignKeys = [
        ForeignKey(
            entity = WatchlistFolder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class WatchlistFund(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val schemeCode: String,

    val folderId: Long
)
