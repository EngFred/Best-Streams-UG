package com.engineerfred.beststreamsug.presentation.browse

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.Language

data class BrowseDirectoryUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val vjs: List<Language> = emptyList(),
    val error: AppError? = null,
)
