package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentSummary

interface BrowseRepository {
    suspend fun getContentByCategory(
        categoryId: Int,
        pageNumber: Int,
        sort: BrowseSort? = null,
    ): AppResult<Page<ContentSummary>>

    suspend fun getContentByLanguage(
        languageId: Int,
        pageNumber: Int,
        sort: BrowseSort? = null,
    ): AppResult<Page<ContentSummary>>
}
