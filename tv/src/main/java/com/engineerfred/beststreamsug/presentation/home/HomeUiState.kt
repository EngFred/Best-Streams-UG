package com.engineerfred.beststreamsug.presentation.home

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.HomeContent

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRecentlyAddedLoading: Boolean = true,
    val isCategoryRailsLoading: Boolean = true,
    val content: HomeContent? = null,
    val error: AppError? = null,
)
