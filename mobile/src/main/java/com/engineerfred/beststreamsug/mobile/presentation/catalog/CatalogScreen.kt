package com.engineerfred.beststreamsug.mobile.presentation.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.presentation.catalog.components.CatalogShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ContentPosterCard
import com.engineerfred.beststreamsug.mobile.ui.components.ContentWideGridCard
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.EmptyState
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicMutedText
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicPrimary
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val PREFETCH_THRESHOLD = 6

@Composable
fun CatalogRoute(
    source: String,
    id: Int,
    title: String,
    filter: String,
    viewModel: CatalogViewModel = hiltViewModel(),
    onContentSelected: (ContentSummary) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreen(
        state = state,
        onRetry = viewModel::refresh,
        onLoadMore = viewModel::loadNextPage,
        onContentSelected = onContentSelected,
        onBack = onBack,
    )
}

@Composable
private fun CatalogScreen(
    state: CatalogUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.items.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CatalogHeader(title = state.title, onBack = onBack)
                    CatalogShimmerSkeleton()
                }
            }
            state.error != null && state.items.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CatalogHeader(title = state.title, onBack = onBack)
                    Box(modifier = Modifier.weight(1f)) {
                        ErrorState(error = state.error, onRetry = onRetry)
                    }
                }
            }
            state.items.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CatalogHeader(title = state.title, onBack = onBack)
                    Box(modifier = Modifier.weight(1f)) {
                        EmptyState(
                            title = "Nothing here yet",
                            message = "Content in this catalog is still being added.",
                        )
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CatalogHeader(title = state.title, onBack = onBack)

                    val showGrid = state.items.size >= 8
                    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                    LaunchedEffect(gridState) {
                        snapshotFlow {
                            val info = gridState.layoutInfo
                            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                            val totalItems = info.totalItemsCount
                            lastVisibleIndex in 0 until totalItems &&
                                lastVisibleIndex >= totalItems - PREFETCH_THRESHOLD
                        }
                            .distinctUntilChanged()
                            .filter { nearEnd -> nearEnd }
                            .collect {
                                if (state.hasMore && !state.isLoadingMore && !state.isLoading) {
                                    onLoadMore()
                                }
                            }
                    }

                    LazyVerticalGrid(
                        columns = if (showGrid) GridCells.Fixed(3) else GridCells.Fixed(1),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 40.dp,
                        ),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(
                            items = state.items,
                            key = { _, item -> "catalog_${item.id}" },
                        ) { _, item ->
                            if (showGrid) {
                                ContentPosterCard(
                                    content = item,
                                    onClick = { onContentSelected(item) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                ContentWideGridCard(
                                    content = item,
                                    onClick = { onContentSelected(item) },
                                )
                            }
                        }
                        if (state.isLoadingMore) {
                            item(
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                LoadingMoreFooter()
                            }
                            if (showGrid) {
                                items(3) {
                                    CatalogGridSkeletonItem()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinematicSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .background(Color(0xFF1B1E24), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onBack,
                )
                .padding(12.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LoadingMoreFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = CinematicPrimary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Loading more...",
            style = MaterialTheme.typography.labelMedium,
            color = CinematicMutedText,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogGridSkeletonItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f),
    ) {
        com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
        )
    }
}