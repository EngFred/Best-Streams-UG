package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentSummary

interface RelatedContentRepository {
    suspend fun getRelatedContent(
        contentId: Int,
        typeId: Int,
        videoType: Int,
        pageNumber: Int,
    ): AppResult<Page<ContentSummary>>
}
