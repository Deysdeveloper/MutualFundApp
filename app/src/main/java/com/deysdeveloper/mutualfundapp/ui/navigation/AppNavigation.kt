package com.deysdeveloper.mutualfundapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavDisplay
import androidx.navigation3.runtime.rememberNavBackStack
import com.deysdeveloper.mutualfundapp.ui.explore.ExploreScreen
import com.deysdeveloper.mutualfundapp.ui.product.ProductScreen
import com.deysdeveloper.mutualfundapp.ui.search.SearchScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.FolderDetailScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.WatchlistScreen

// ─── Bottom-nav tabs ──────────────────────────────────────────────────────────

private val bottomNavRoutes = listOf(ExploreRoute, SearchRoute, WatchlistRoute)

private val bottomNavLabels = mapOf(
    ExploreRoute::class to "Explore",
    SearchRoute::class to "Search",
    WatchlistRoute::class to "Watchlist"
)

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(ExploreRoute)

    val currentRoot by remember {
        derivedStateOf {
            backStack.lastOrNull { it is ExploreRoute || it is SearchRoute || it is WatchlistRoute }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoot,
                onTabSelected = { route ->
                    // Clear back to the selected root tab; avoid duplicates
                    if (backStack.lastOrNull() != route) {
                        backStack.removeAll {
                            it is ExploreRoute || it is SearchRoute || it is WatchlistRoute ||
                                    it is ProductRoute || it is FolderRoute
                        }
                        backStack.add(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is ExploreRoute -> NavEntry(key) {
                            ExploreScreen(
                                onNavigateToProduct = { schemeCode ->
                                    backStack.add(ProductRoute(schemeCode))
                                },
                                onNavigateToSearch = {
                                    backStack.add(SearchRoute)
                                }
                            )
                        }

                        is SearchRoute -> NavEntry(key) {
                            SearchScreen(
                                onNavigateToProduct = { schemeCode ->
                                    backStack.add(ProductRoute(schemeCode))
                                },
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }

                        is WatchlistRoute -> NavEntry(key) {
                            WatchlistScreen(
                                onNavigateToFolder = { folderId, folderName ->
                                    backStack.add(FolderRoute(folderId, folderName))
                                }
                            )
                        }

                        is ProductRoute -> NavEntry(key) {
                            ProductScreen(
                                schemeCode = key.schemeCode,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }

                        is FolderRoute -> NavEntry(key) {
                            FolderDetailScreen(
                                folderId = key.folderId,
                                folderName = key.folderName,
                                onBack = { backStack.removeLastOrNull() },
                                onNavigateToProduct = { schemeCode ->
                                    backStack.add(ProductRoute(schemeCode))
                                }
                            )
                        }

                        else -> NavEntry(key) {
                            // Fallback: should never reach here
                            ExploreScreen(
                                onNavigateToProduct = { backStack.add(ProductRoute(it)) },
                                onNavigateToSearch = { backStack.add(SearchRoute) }
                            )
                        }
                    }
                }
            )
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(
    currentRoute: Any?,
    onTabSelected: (Any) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute is ExploreRoute,
            onClick = { onTabSelected(ExploreRoute) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = currentRoute is SearchRoute,
            onClick = { onTabSelected(SearchRoute) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = currentRoute is WatchlistRoute,
            onClick = { onTabSelected(WatchlistRoute) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
            label = { Text("Watchlist") }
        )
    }
}
