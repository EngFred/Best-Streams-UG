package com.engineerfred.beststreamsug.mobile.presentation.series

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.presentation.series.components.SeriesCategoryRail
import com.engineerfred.beststreamsug.mobile.presentation.series.components.SeriesHeroCarousel
import com.engineerfred.beststreamsug.mobile.presentation.series.components.SeriesShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState

@Composable
fun SeriesRoute(
    viewModel: SeriesViewModel = hiltViewModel(),
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenSeriesHome: (sectionId: Int, title: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SeriesScreen(
        state = state,
        onRetry = viewModel::refresh,
        onContentSelected = onContentSelected,
        onOpenCategory = onOpenCategory,
        onOpenSeriesHome = onOpenSeriesHome,
    )
}

@Composable
private fun SeriesScreen(
    state: SeriesUiState,
    onRetry: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onOpenCategory: (categoryId: Int, title: String) -> Unit,
    onOpenSeriesHome: (sectionId: Int, title: String) -> Unit,
) {
    val content = state.content

    when {
        state.isLoading && content == null -> SeriesShimmerSkeleton()
        content != null -> {
            val banners = (content.banners as? AppResult.Success)?.data.orEmpty()
            val categoryRails = content.categoryRails

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                item {
                    SeriesHeroCarousel(
                        banners = banners,
                        onContentSelected = onContentSelected,
                    )
                }

                items(
                    items = categoryRails,
                    key = { "series_rail_${it.categoryId}" },
                ) { rail ->
                    SeriesCategoryRail(
                        rail = rail,
                        onContentSelected = onContentSelected,
                        onOpenCategory = onOpenCategory,
                    )
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                ErrorState(
                    error = state.error,
                    onRetry = onRetry,
                )
            }
        }
    }
}