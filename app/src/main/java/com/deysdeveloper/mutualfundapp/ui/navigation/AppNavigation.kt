package com.deysdeveloper.mutualfundapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.deysdeveloper.mutualfundapp.ui.explore.ExploreScreen
import com.deysdeveloper.mutualfundapp.ui.product.ProductScreen
import com.deysdeveloper.mutualfundapp.ui.search.SearchScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.FolderDetailScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.WatchlistScreen

// ─── Bottom nav items ─────────────────────────────────────────────────────────

private data class NavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem(Route.Explore,   "Explore",   Icons.Rounded.Explore),
    NavItem(Route.Search,    "Search",    Icons.Rounded.Search),
    NavItem(Route.Watchlist, "Watchlist", Icons.Rounded.Bookmarks),
)

// ─── Navigation host ──────────────────────────────────────────────────────────

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(Route.Explore)

    // The "active" bottom-nav tab is the deepest tab-level route in the stack
    val selectedRootRoute by remember {
        derivedStateOf {
            backStack.filterIsInstance<Route>()
                .lastOrNull { it is Route.Explore || it is Route.Search || it is Route.Watchlist }
                ?: Route.Explore
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedRootRoute == item.route,
                        onClick = {
                            if (selectedRootRoute == item.route) {
                                // Same tab tapped – pop to root in a single state mutation
                                if (backStack.size > 1) {
                                    backStack.subList(1, backStack.size).clear()
                                }
                            } else {
                                // Different tab – clear stack and go to new root
                                backStack.clear()
                                backStack.add(item.route)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E3A8A),
                            selectedTextColor = Color(0xFF1E3A8A),
                            indicatorColor = Color(0xFF1E3A8A).copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
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
                entryProvider = entryProvider {

                    entry<Route.Explore> {
                        ExploreScreen(
                            onNavigateToProduct = { schemeCode ->
                                backStack.add(Route.Product(schemeCode))
                            },
                            onNavigateToSearch = {
                                backStack.add(Route.Search)
                            }
                        )
                    }

                    entry<Route.Search> {
                        SearchScreen(
                            onNavigateToProduct = { schemeCode ->
                                backStack.add(Route.Product(schemeCode))
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Route.Watchlist> {
                        WatchlistScreen(
                            onNavigateToFolder = { folderId, folderName ->
                                backStack.add(Route.Folder(folderId, folderName))
                            }
                        )
                    }

                    entry<Route.Product> { key ->
                        ProductScreen(
                            schemeCode = key.schemeCode,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Route.Folder> { key ->
                        FolderDetailScreen(
                            folderId = key.folderId,
                            folderName = key.folderName,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToProduct = { schemeCode ->
                                backStack.add(Route.Product(schemeCode))
                            }
                        )
                    }
                }
            )
        }
    }
}
