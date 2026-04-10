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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deysdeveloper.mutualfundapp.ui.explore.ExploreScreen
import com.deysdeveloper.mutualfundapp.ui.product.ProductScreen
import com.deysdeveloper.mutualfundapp.ui.search.SearchScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.FolderDetailScreen
import com.deysdeveloper.mutualfundapp.ui.watchlist.WatchlistScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Top-level tab routes shown in the bottom nav bar
    val topLevelRoutes = listOf(Routes.EXPLORE, Routes.SEARCH, Routes.WATCHLIST)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.EXPLORE } == true,
                    onClick = {
                        navController.navigate(Routes.EXPLORE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.SEARCH } == true,
                    onClick = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.WATCHLIST } == true,
                    onClick = {
                        navController.navigate(Routes.WATCHLIST) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                    label = { Text("Watchlist") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.EXPLORE
            ) {
                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        onNavigateToProduct = { schemeCode ->
                            navController.navigate(Routes.product(schemeCode))
                        },
                        onNavigateToSearch = {
                            navController.navigate(Routes.SEARCH)
                        }
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onNavigateToProduct = { schemeCode ->
                            navController.navigate(Routes.product(schemeCode))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.WATCHLIST) {
                    WatchlistScreen(
                        onNavigateToFolder = { folderId, folderName ->
                            navController.navigate(Routes.folder(folderId, folderName))
                        }
                    )
                }

                composable(
                    route = Routes.PRODUCT,
                    arguments = listOf(
                        navArgument("schemeCode") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val schemeCode = backStackEntry.arguments?.getString("schemeCode") ?: return@composable
                    ProductScreen(
                        schemeCode = schemeCode,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.FOLDER,
                    arguments = listOf(
                        navArgument("folderId") { type = NavType.LongType },
                        navArgument("folderName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val folderId = backStackEntry.arguments?.getLong("folderId") ?: return@composable
                    val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                    FolderDetailScreen(
                        folderId = folderId,
                        folderName = folderName.replace("%2F", "/").replace("%20", " "),
                        onBack = { navController.popBackStack() },
                        onNavigateToProduct = { schemeCode ->
                            navController.navigate(Routes.product(schemeCode))
                        }
                    )
                }
            }
        }
    }
}
