package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class GetLanguagesUseCase @Inject constructor(
    private val repository: MetadataRepository,
) {
    suspend operator fun invoke(): AppResult<List<Language>> = repository.getLanguages()
}
