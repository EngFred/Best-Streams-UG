package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.SearchRepository
import javax.inject.Inject

class SearchContentUseCase @Inject constructor(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(
        keyword: String,
        languageId: Int? = null,
        pageNumber: Int = 1,
    ): AppResult<Page<ContentSummary>> = repository.search(keyword, languageId, pageNumber)
}
