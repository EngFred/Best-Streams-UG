package com.engineerfred.beststreamsug.domain.model

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page

data class HomeCategoryRail(
    val category: Category,
    val content: AppResult<Page<ContentSummary>>,
)
