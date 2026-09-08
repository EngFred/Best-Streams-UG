package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.repository.ContentDetailsRepository
import javax.inject.Inject

class GetContentDetailsUseCase @Inject constructor(
    private val repository: ContentDetailsRepository,
) {
    suspend operator fun invoke(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<ContentDetails> = repository.getContentDetails(contentId, typeId, videoType)
}
