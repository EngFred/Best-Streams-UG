package com.engineerfred.beststreamsug.presentation.series

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.SeriesHomeContent

data class SeriesUiState(
    val isLoading: Boolean = true,
    val isCategoryRailsLoading: Boolean = true,
    val content: SeriesHomeContent? = null,
    val error: AppError? = null,
)
