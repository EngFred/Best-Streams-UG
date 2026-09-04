package com.engineerfred.beststreamsug.presentation.browse

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.ContentSummary

data class CatalogUiState(
    val title: String = "",
    val source: String = "",
    val filter: String = "",
    val items: List<ContentSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: AppError? = null,
)
