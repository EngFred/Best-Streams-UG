package com.engineerfred.beststreamsug.presentation.browse

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.ContentSummary

data class SectionGridUiState(
    val title: String = "",
    val subtitle: String = "",
    val items: List<ContentSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
)