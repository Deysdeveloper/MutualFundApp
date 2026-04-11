package com.deysdeveloper.mutualfundapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route keys for Navigation 3.
 * Each subtype is @Serializable so Nav3 can save/restore the back stack.
 */
@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Explore : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Watchlist : Route

    @Serializable
    data class Product(val schemeCode: String) : Route

    @Serializable
    data class Folder(val folderId: Long, val folderName: String) : Route
}
