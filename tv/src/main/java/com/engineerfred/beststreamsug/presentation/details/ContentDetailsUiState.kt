package com.engineerfred.beststreamsug.presentation.details

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.Episode

data class ContentDetailsUiState(
    val isLoading: Boolean = true,
    val details: ContentDetails? = null,
    val error: AppError? = null,
    val selectedSeasonId: Int? = null,
    val episodes: List<Episode> = emptyList(),
    val isLoadingEpisodes: Boolean = false,
    val episodesError: AppError? = null,
    val relatedContent: List<ContentSummary> = emptyList(),
    val isLoadingRelated: Boolean = false,
    val relatedError: AppError? = null,
)
