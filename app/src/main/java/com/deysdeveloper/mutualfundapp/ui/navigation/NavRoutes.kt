package com.deysdeveloper.mutualfundapp.ui.navigation

/**
 * Navigation route string constants for Navigation Compose.
 */
object Routes {
    const val EXPLORE = "explore"
    const val SEARCH = "search"
    const val WATCHLIST = "watchlist"

    // Routes with arguments
    const val PRODUCT = "product/{schemeCode}"
    const val FOLDER = "folder/{folderId}/{folderName}"

    // Builders for navigating with arguments
    fun product(schemeCode: String) = "product/$schemeCode"
    fun folder(folderId: Long, folderName: String) =
        "folder/$folderId/${folderName.encodeForRoute()}"
}

/** Percent-encodes slashes and spaces so folder names are safe in routes. */
private fun String.encodeForRoute(): String =
    this.replace("/", "%2F").replace(" ", "%20")
