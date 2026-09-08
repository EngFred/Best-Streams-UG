package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: MetadataRepository,
) {
    suspend operator fun invoke(): AppResult<List<Category>> = repository.getCategories()
}
