package com.deysdeveloper.mutualfundapp.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation 3 route keys.
 * Each route is a @Serializable type so the back stack can survive process death.
 */

@Serializable
data object ExploreRoute

@Serializable
data object SearchRoute

@Serializable
data object WatchlistRoute

@Serializable
data class ProductRoute(val schemeCode: String)

@Serializable
data class FolderRoute(val folderId: Long, val folderName: String)
