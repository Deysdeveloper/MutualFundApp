package com.deysdeveloper.mutualfundapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_funds")
data class CachedFund(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Category key used to search — e.g. "index", "bluechip", "tax", "large cap". */
    val category: String,
    val schemeCode: String,
    val fundName: String,
    val nav: String
)
