package com.engineerfred.beststreamsug.mobile.presentation.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.engineerfred.beststreamsug.mobile.ui.theme.CinematicSurface

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
                    LaunchedEffect(gridState, state.items.size) {
                        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@LaunchedEffect
                        if (lastVisible >= state.items.size - 6 && state.hasMore && !state.isLoadingMore) {
                            onLoadMore()
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
                                    showTitle = false,
                                )
                            } else {
                                ContentWideGridCard(
                                    content = item,
                                    onClick = { onContentSelected(item) },
                                )
                            }
                        }
                        if (state.isLoadingMore) {
                            items(6) {
                                CatalogListSkeletonItem()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogListSkeletonItem() {
    Column(modifier = Modifier.fillMaxWidth()) {
        com.engineerfred.beststreamsug.mobile.ui.components.shimmer.ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(12.dp),
        )
    }
}