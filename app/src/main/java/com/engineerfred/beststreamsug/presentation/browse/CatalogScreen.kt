package com.engineerfred.beststreamsug.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.presentation.common.PosterCard
import com.engineerfred.beststreamsug.presentation.common.skeletons.PosterGridLoadingSkeleton

@Composable
fun CatalogRoute(
    onBack: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreen(state, onBack, viewModel::loadNextPage, onContentSelected)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    when {
        state.isLoading -> PosterGridLoadingSkeleton()
        state.error != null && state.items.isEmpty() -> CatalogError(state.error, onLoadMore)
        else -> {
            val rows = state.items.chunked(COLUMNS)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = subtitle(state),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                itemsIndexed(rows) { index, row ->
                    CatalogRow(row, onContentSelected)
                    if (index == rows.lastIndex) {
                        LaunchedEffect(state.items.size, state.hasMore, state.error) {
                            onLoadMore()
                        }
                    }
                }
                if (state.isLoadingMore) {
                    item {
                        Text(
                            text = "Loading more movies…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (!state.hasMore && state.items.isNotEmpty()) {
                    item {
                        Text(
                            text = "You have reached the end of the catalog",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                } else if (state.error != null) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Could not load more items",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                            Card(
                                onClick = onLoadMore,
                                shape = CardDefaults.shape(
                                    shape = RoundedCornerShape(8.dp),
                                    focusedShape = RoundedCornerShape(8.dp),
                                ),
                                scale = CardDefaults.scale(
                                    scale = 1f,
                                    focusedScale = 1.08f,
                                ),
                                colors = CardDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text(
                                    text = "Retry",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CatalogRow(
    content: List<ContentSummary>,
    onContentSelected: (ContentSummary) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content.forEach { item ->
            PosterCard(
                content = item,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2f / 3f),
                onClick = { onContentSelected(item) },
            )
        }
        repeat(COLUMNS - content.size) {
            Column(modifier = Modifier.weight(1f)) {}
        }
    }
}

private fun subtitle(state: CatalogUiState): String = when (state.source) {
    "vj" -> "Dubbed by ${state.title}"
    "category" -> when (state.filter) {
        com.engineerfred.beststreamsug.presentation.navigation.AppDestination.Catalog.FILTER_SERIES ->
            "Series in ${state.title}"
        com.engineerfred.beststreamsug.presentation.navigation.AppDestination.Catalog.FILTER_ALL ->
            "All content in ${state.title}"
        else -> "Movies in ${state.title}"
    }
    else -> "Content in ${state.title}"
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CatalogError(
    error: AppError,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = error.userMessage(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            onClick = onRetry,
            shape = CardDefaults.shape(
                shape = RoundedCornerShape(8.dp),
                focusedShape = RoundedCornerShape(8.dp),
            ),
            scale = CardDefaults.scale(
                scale = 1f,
                focusedScale = 1.08f,
            ),
            colors = CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

private fun AppError.userMessage(): String = when (this) {
    AppError.NetworkUnavailable -> "Network unavailable"
    AppError.Timeout -> "The service took too long to respond"
    is AppError.Http -> "The service is temporarily unavailable"
    is AppError.Serialization, is AppError.InvalidResponse -> "The service returned invalid data"
    AppError.EmptyResponse -> "No movies are available"
    is AppError.Unknown -> "Unable to load movies"
}

private const val COLUMNS = 5
