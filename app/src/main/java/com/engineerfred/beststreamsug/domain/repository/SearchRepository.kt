package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentSummary

interface SearchRepository {
    suspend fun search(
        keyword: String,
        languageId: Int? = null,
        pageNumber: Int = 1,
    ): AppResult<Page<ContentSummary>>
}
