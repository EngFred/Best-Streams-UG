package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.RelatedContentRepository
import javax.inject.Inject

class GetRelatedContentUseCase @Inject constructor(
    private val repository: RelatedContentRepository,
) {
    suspend operator fun invoke(
        contentId: Int,
        typeId: Int,
        videoType: Int,
        pageNumber: Int,
    ): AppResult<Page<ContentSummary>> = repository.getRelatedContent(
        contentId,
        typeId,
        videoType,
        pageNumber,
    )
}
