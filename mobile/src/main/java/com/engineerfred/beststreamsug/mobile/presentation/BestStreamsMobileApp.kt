package com.engineerfred.beststreamsug.mobile.presentation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.engineerfred.beststreamsug.mobile.presentation.browse.BrowseRoute
import com.engineerfred.beststreamsug.mobile.presentation.catalog.CatalogRoute
import com.engineerfred.beststreamsug.mobile.presentation.details.DetailsRoute
import com.engineerfred.beststreamsug.mobile.presentation.home.HomeRoute
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileDestination
import com.engineerfred.beststreamsug.mobile.presentation.navigation.MobileTab
import com.engineerfred.beststreamsug.mobile.presentation.navigation.asContentKind
import com.engineerfred.beststreamsug.mobile.presentation.player.PlayerRoute
import com.engineerfred.beststreamsug.mobile.presentation.search.SearchRoute
import com.engineerfred.beststreamsug.mobile.presentation.series.SeriesRoute
import com.engineerfred.beststreamsug.domain.model.ContentSummary

@Composable
fun BestStreamsMobileApp(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = MobileTab.entries.any { tab ->
        currentDestination?.route == tab.destination.route
    }

    val openDetails: (ContentSummary) -> Unit = { content ->
        navController.navigate(
            MobileDestination.ContentDetails.createRoute(content.id, content.kind),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = MobileDestination.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn() + slideInHorizontally { it / 3 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 3 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 3 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 3 } },
        ) {
            composable(route = MobileDestination.Home.route) {
                HomeRoute(
                    onContentSelected = openDetails,
                    onOpenCategory = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute("category", id, title),
                        )
                    },
                    onOpenSection = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute("section", id, title),
                        )
                    },
                    onSearchSelected = { navController.navigate(MobileDestination.Search.route) },
                )
            }
            composable(route = MobileDestination.Series.route) {
                SeriesRoute(
                    onContentSelected = openDetails,
                    onOpenCategory = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute(
                                source = "category",
                                id = id,
                                title = title,
                                filter = MobileDestination.Catalog.FILTER_SERIES,
                            ),
                        )
                    },
                    onOpenSeriesHome = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute("section", id, title),
                        )
                    },
                )
            }
            composable(route = MobileDestination.Browse.route) {
                BrowseRoute(
                    onCategorySelected = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute("category", id, title),
                        )
                    },
                    onLanguageSelected = { id, title ->
                        navController.navigate(
                            MobileDestination.Catalog.createRoute("language", id, title),
                        )
                    },
                )
            }
            composable(route = MobileDestination.Search.route) {
                SearchRoute(
                    onContentSelected = openDetails,
                )
            }
            composable(
                route = MobileDestination.Catalog.route,
                arguments = listOf(
                    navArgument(MobileDestination.Catalog.sourceArg) { type = NavType.StringType },
                    navArgument(MobileDestination.Catalog.idArg) { type = NavType.IntType },
                    navArgument(MobileDestination.Catalog.titleArg) { type = NavType.StringType },
                    navArgument(MobileDestination.Catalog.filterArg) {
                        type = NavType.StringType
                        defaultValue = MobileDestination.Catalog.FILTER_ALL
                    },
                ),
            ) { backStackEntry ->
                val args = backStackEntry.arguments ?: return@composable
                CatalogRoute(
                    source = args.getString(MobileDestination.Catalog.sourceArg).orEmpty(),
                    id = args.getInt(MobileDestination.Catalog.idArg),
                    title = args.getString(MobileDestination.Catalog.titleArg).orEmpty(),
                    filter = args.getString(MobileDestination.Catalog.filterArg)
                        ?: MobileDestination.Catalog.FILTER_ALL,
                    onContentSelected = openDetails,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = MobileDestination.ContentDetails.route,
                arguments = listOf(
                    navArgument(MobileDestination.ContentDetails.contentIdArg) {
                        type = NavType.IntType
                    },
                    navArgument(MobileDestination.ContentDetails.kindArg) {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val args = backStackEntry.arguments ?: return@composable
                DetailsRoute(
                    contentId = args.getInt(MobileDestination.ContentDetails.contentIdArg),
                    contentKind = args.getString(MobileDestination.ContentDetails.kindArg)
                        ?.asContentKind()
                        ?: return@composable,
                    onBack = { navController.popBackStack() },
                    onPlay = { url, title, meta, poster ->
                        navController.navigate(
                            MobileDestination.Player.createRoute(
                                url = url,
                                title = title,
                                meta = meta,
                                poster = poster,
                            ),
                        )
                    },
                    onContentSelected = openDetails,
                )
            }
            composable(
                route = MobileDestination.Player.route,
                arguments = listOf(
                    navArgument(MobileDestination.Player.urlArg) {
                        type = NavType.StringType
                    },
                    navArgument(MobileDestination.Player.titleArg) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(MobileDestination.Player.metaArg) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(MobileDestination.Player.posterArg) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val args = backStackEntry.arguments ?: return@composable
                val url = args.getString(MobileDestination.Player.urlArg) ?: return@composable
                PlayerRoute(
                    url = url,
                    title = args.getString(MobileDestination.Player.titleArg)
                        ?.takeUnless { it.isBlank() },
                    meta = args.getString(MobileDestination.Player.metaArg)
                        ?.takeUnless { it.isBlank() },
                    poster = args.getString(MobileDestination.Player.posterArg)
                        ?.takeUnless { it.isBlank() },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (showBottomBar) {
            BottomTabBar(
                currentTab = currentTab(currentDestination),
                onTabSelected = { tab -> navigateToTab(navController, tab) },
            )
        }
    }
}

private fun currentTab(destination: androidx.navigation.NavDestination?): MobileTab? {
    return MobileTab.entries.firstOrNull { tab ->
        destination?.route == tab.destination.route
    }
}

private fun navigateToTab(navController: NavHostController, tab: MobileTab) {
    navController.navigate(tab.destination.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomTabBar(
    currentTab: MobileTab?,
    onTabSelected: (MobileTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        MobileTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(text = tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}