package com.engineerfred.beststreamsug.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.presentation.common.PosterCard
import com.engineerfred.beststreamsug.presentation.common.skeletons.PosterGridLoadingSkeleton

@Composable
fun SectionGridRoute(
    onBack: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
    viewModel: SectionGridViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SectionGridScreen(state, onBack, onContentSelected)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SectionGridScreen(
    state: SectionGridUiState,
    onBack: () -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    when {
        state.isLoading -> PosterGridLoadingSkeleton()
        state.error != null && state.items.isEmpty() -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.error.userMessage(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
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
                        if (state.subtitle.isNotBlank()) {
                            Text(
                                text = state.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                itemsIndexed(rows) { index, row ->
                    SectionGridRow(row, onContentSelected)
                }
                if (state.items.isEmpty()) {
                    item {
                        Text(
                            text = "Nothing to see here yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SectionGridRow(
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

private fun AppError.userMessage(): String = when (this) {
    AppError.NetworkUnavailable -> "Network unavailable"
    AppError.Timeout -> "The service took too long to respond"
    is AppError.Http -> "The service is temporarily unavailable"
    is AppError.Serialization, is AppError.InvalidResponse -> "The service returned invalid data"
    AppError.EmptyResponse -> "No content is available"
    is AppError.Unknown -> "Unable to load content"
}

private const val COLUMNS = 5