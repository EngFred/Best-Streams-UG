package com.engineerfred.beststreamsug.domain.model

import com.engineerfred.beststreamsug.core.common.AppResult

data class RecentlyAddedRails(
    val movies: AppResult<List<ContentSummary>>,
    val series: AppResult<List<ContentSummary>>,
)
