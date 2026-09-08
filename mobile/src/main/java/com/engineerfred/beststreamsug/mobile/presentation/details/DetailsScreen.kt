package com.engineerfred.beststreamsug.mobile.presentation.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.mobile.presentation.details.components.DetailsContent
import com.engineerfred.beststreamsug.mobile.presentation.details.components.DetailsShimmerSkeleton
import com.engineerfred.beststreamsug.mobile.ui.components.ErrorState

@Composable
fun DetailsRoute(
    contentId: Int,
    contentKind: ContentKind,
    viewModel: DetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailsScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onSelectSeason = viewModel::selectSeason,
        onPlay = onPlay,
        onContentSelected = onContentSelected,
    )
}

@Composable
private fun DetailsScreen(
    state: DetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectSeason: (Int) -> Unit,
    onPlay: (url: String, title: String?, meta: String?, poster: String?) -> Unit,
    onContentSelected: (ContentSummary) -> Unit,
) {
    val details = state.details

    when {
        state.isLoading && details == null -> DetailsShimmerSkeleton(onBack = onBack)

        details != null -> {
            DetailsContent(
                state = state,
                details = details,
                onBack = onBack,
                onSelectSeason = onSelectSeason,
                onPlay = onPlay,
                onContentSelected = onContentSelected,
            )
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