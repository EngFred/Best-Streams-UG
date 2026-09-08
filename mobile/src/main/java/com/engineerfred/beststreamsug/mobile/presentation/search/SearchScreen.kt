package com.engineerfred.beststreamsug.mobile.presentation.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.presentation.search.components.SearchPromptState
import com.engineerfred.beststreamsug.mobile.presentation.search.components.SearchShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.presentation.search.components.SearchTopBar
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState
import com.engineerfred.beststreamsug.mobile.ui.components.EmptyState

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onContentSelected: (ContentSummary) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onRetry = viewModel::retry,
        onContentSelected = onContentSelected,
    )
}

@Composable
private fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchTopBar(
            query = state.query,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
        )

        when {
            state.isLoading -> SearchShimmerSkeleton()
            state.query.isBlank() && !state.hasSearched -> {
                SearchPromptState(
                    modifier = Modifier.weight(1f),
                )
            }
            state.error != null && state.results.isEmpty() -> {
                Box(modifier = Modifier.weight(1f)) {
                    ErrorState(
                        error = state.error,
                        onRetry = onRetry,
                    )
                }
            }
            state.results.isEmpty() -> {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "No results",
                        message = "Nothing found for \"${state.query}\". Try a different keyword.",
                    )
                }
            }
            else -> SearchResultsGrid(
                results = state.results,
                onContentSelected = onContentSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsGrid(
    results: List<ContentSummary>,
    onContentSelected: (ContentSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = results,
            key = { "search_${it.id}" },
        ) { content ->
            com.engineerfred.beststreamsug.mobile.ui.components.ContentPosterCard(
                content = content,
                onClick = { onContentSelected(content) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}