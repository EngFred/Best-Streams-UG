package com.engineerfred.beststreamsug.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.presentation.details.ContentDetailsRoute
import com.engineerfred.beststreamsug.presentation.browse.BrowseDirectoryRoute
import com.engineerfred.beststreamsug.presentation.browse.CatalogRoute
import com.engineerfred.beststreamsug.presentation.browse.SectionGridRoute
import com.engineerfred.beststreamsug.presentation.home.HomeRoute
import com.engineerfred.beststreamsug.presentation.player.PlayerRoute
import com.engineerfred.beststreamsug.presentation.search.SearchRoute
import com.engineerfred.beststreamsug.presentation.series.SeriesRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val openDetails: (ContentSummary) -> Unit = { content ->
        if (content.kind != ContentKind.UNKNOWN) {
            navController.navigate(
                AppDestination.ContentDetails.createRoute(content.id, content.kind),
            )
        }
    }
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier,
    ) {
        composable(
            route = AppDestination.Home.route,
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            HomeRoute(
                onBrowseSelected = {
                    navController.navigate(AppDestination.BrowseDirectory.route)
                },
                onSeriesSelected = {
                    navController.navigate(AppDestination.Series.route)
                },
                onSearchSelected = {
                    navController.navigate(AppDestination.Search.route)
                },
                onContentSelected = openDetails,
                onOpenSection = { title ->
                    navController.navigate(
                        AppDestination.SectionGrid.createRoute(
                            AppDestination.SectionGrid.SCREEN_HOME,
                            title,
                        ),
                    )
                },
                onOpenCategory = { categoryId, name ->
                    navController.navigate(
                        AppDestination.Catalog.createRoute(
                            source = "category",
                            id = categoryId,
                            title = name,
                            filter = AppDestination.Catalog.FILTER_MOVIES,
                        ),
                    )
                },
                onOpenVj = { vjId, name ->
                    navController.navigate(
                        AppDestination.Catalog.createRoute(
                            source = "vj",
                            id = vjId,
                            title = name,
                            filter = AppDestination.Catalog.FILTER_ALL,
                        ),
                    )
                },
            )
        }
        composable(
            route = AppDestination.Search.route,
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            SearchRoute(
                onBack = { navController.popBackStack() },
                onContentSelected = openDetails,
            )
        }
        composable(
            route = AppDestination.Series.route,
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            SeriesRoute(
                onBack = { navController.popBackStack() },
                onContentSelected = openDetails,
                onOpenSection = { title ->
                    navController.navigate(
                        AppDestination.SectionGrid.createRoute(
                            AppDestination.SectionGrid.SCREEN_SERIES,
                            title,
                        ),
                    )
                },
                onOpenCategory = { categoryId, name ->
                    navController.navigate(
                        AppDestination.Catalog.createRoute(
                            source = "category",
                            id = categoryId,
                            title = name,
                            filter = AppDestination.Catalog.FILTER_SERIES,
                        ),
                    )
                },
            )
        }
        composable(
            route = AppDestination.BrowseDirectory.route,
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            BrowseDirectoryRoute(
                onBack = { navController.popBackStack() },
                onCatalogSelected = { source, id, title ->
                    navController.navigate(AppDestination.Catalog.createRoute(source, id, title))
                },
            )
        }
        composable(
            route = AppDestination.Catalog.route,
            arguments = listOf(
                navArgument(AppDestination.Catalog.sourceArgument) {
                    type = NavType.StringType
                },
                navArgument(AppDestination.Catalog.idArgument) {
                    type = NavType.IntType
                },
                navArgument(AppDestination.Catalog.titleArgument) {
                    type = NavType.StringType
                },
                navArgument(AppDestination.Catalog.filterArgument) {
                    type = NavType.StringType
                    defaultValue = AppDestination.Catalog.FILTER_MOVIES
                },
            ),
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            CatalogRoute(
                onBack = { navController.popBackStack() },
                onContentSelected = openDetails,
            )
        }
        composable(
            route = AppDestination.SectionGrid.route,
            arguments = listOf(
                navArgument(AppDestination.SectionGrid.screenArgument) {
                    type = NavType.IntType
                },
                navArgument(AppDestination.SectionGrid.titleArgument) {
                    type = NavType.StringType
                },
            ),
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) {
            SectionGridRoute(
                onBack = { navController.popBackStack() },
                onContentSelected = openDetails,
            )
        }
        composable(
            route = AppDestination.ContentDetails.route,
            arguments = listOf(
                navArgument(AppDestination.ContentDetails.contentIdArgument) {
                    type = NavType.IntType
                },
                navArgument(AppDestination.ContentDetails.contentKindArgument) {
                    type = NavType.StringType
                },
            ),
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) { backStackEntry ->
            ContentDetailsRoute(
                onBack = { navController.popBackStack() },
                onPlay = { url, details ->
                    navController.navigate(
                        AppDestination.Player.createRoute(
                            url = url,
                            title = details.summary.title,
                            meta = buildPlayerMeta(details),
                            poster = details.summary.thumbnailUrl,
                        ),
                    )
                },
                onContentSelected = openDetails,
            )
        }
        composable(
            route = AppDestination.Player.route,
            arguments = listOf(
                navArgument(AppDestination.Player.urlArgument) {
                    type = NavType.StringType
                },
                navArgument(AppDestination.Player.titleArgument) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(AppDestination.Player.metaArgument) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(AppDestination.Player.posterArgument) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
            enterTransition = { navEnterTransition() },
            exitTransition = { navExitTransition() },
            popEnterTransition = { navPopEnterTransition() },
            popExitTransition = { navPopExitTransition() },
        ) { backStackEntry ->
            val url = backStackEntry.arguments
                ?.getString(AppDestination.Player.urlArgument)
                ?: return@composable
            PlayerRoute(
                url = url,
                title = backStackEntry.arguments
                    ?.getString(AppDestination.Player.titleArgument)
                    ?.takeIf { it.isNotBlank() },
                meta = backStackEntry.arguments
                    ?.getString(AppDestination.Player.metaArgument)
                    ?.takeIf { it.isNotBlank() },
                poster = backStackEntry.arguments
                    ?.getString(AppDestination.Player.posterArgument)
                    ?.takeIf { it.isNotBlank() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun buildPlayerMeta(details: ContentDetails): String {
    val meta = buildList {
        details.summary.vjName?.takeIf { it.isNotBlank() }?.let(::add)
        details.summary.releaseDate?.takeIf { it.isNotBlank() }
            ?.let { add(it.take(4)) }
        details.categoryNames.take(2).forEach(::add)
    }
    return meta.joinToString("  •  ")
}
