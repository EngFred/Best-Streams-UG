package com.engineerfred.beststreamsug.mobile.presentation.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox

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
            val categoryRails = content.categoryRails.filter { rail ->
                (rail.content as? AppResult.Success)?.data?.isNotEmpty() == true
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                item {
                    SeriesHeroCarousel(
                        banners = banners,
                        onContentSelected = onContentSelected,
                    )
                }

                if (state.isCategoryRailsLoading) {
                    item {
                        SeriesCategoryRailsLoadingBlock()
                    }
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

@Composable
private fun SeriesCategoryRailsLoadingBlock() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ShimmerBox(
                modifier = Modifier.height(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(5) {
                item {
                    ShimmerBox(
                        modifier = Modifier
                            .height(180.dp)
                            .width(120.dp),
                    )
                }
            }
        }
    }
}