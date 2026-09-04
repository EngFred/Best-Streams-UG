package com.engineerfred.beststreamsug.domain.model

import com.engineerfred.beststreamsug.core.common.AppResult

data class SeriesHomeContent(
    val banners: AppResult<List<Banner>>,
    val sections: AppResult<List<ContentSection>> = AppResult.Success(emptyList()),
    val categoryRails: List<SeriesCategoryRail>,
)

data class SeriesCategoryRail(
    val categoryId: Int,
    val categoryName: String,
    val content: AppResult<List<ContentSummary>>,
)
