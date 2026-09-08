package com.engineerfred.beststreamsug.domain.model

import com.engineerfred.beststreamsug.core.common.AppResult

data class HomeContent(
    val banners: AppResult<List<Banner>>,
    val categories: AppResult<List<Category>>,
    val languages: AppResult<List<Language>>,
    val categoryRails: AppResult<List<HomeCategoryRail>>,
    val sections: AppResult<List<ContentSection>> = AppResult.Success(emptyList()),
    val recentlyAddedMovies: AppResult<List<ContentSummary>> = AppResult.Success(emptyList()),
    val recentlyAddedSeries: AppResult<List<ContentSummary>> = AppResult.Success(emptyList()),
)
