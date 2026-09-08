package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import javax.inject.Inject

class GetLanguageContentUseCase @Inject constructor(
    private val repository: BrowseRepository,
) {
    suspend operator fun invoke(
        languageId: Int,
        pageNumber: Int,
        sort: BrowseSort? = null,
    ): AppResult<Page<ContentSummary>> = repository.getContentByLanguage(languageId, pageNumber, sort)
}
