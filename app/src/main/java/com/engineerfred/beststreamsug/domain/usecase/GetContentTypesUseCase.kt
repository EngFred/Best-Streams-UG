package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class GetContentTypesUseCase @Inject constructor(
    private val repository: MetadataRepository,
) {
    suspend operator fun invoke(): AppResult<List<ContentType>> = repository.getContentTypes()
}
