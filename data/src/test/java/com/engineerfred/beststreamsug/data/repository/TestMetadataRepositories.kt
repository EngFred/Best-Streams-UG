package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository

internal fun emptyMetadataRepository() = object : MetadataRepository {
    override suspend fun getCategories(): AppResult<List<Category>> =
        AppResult.Success(emptyList())

    override suspend fun getContentTypes(): AppResult<List<ContentType>> =
        AppResult.Success(emptyList())

    override suspend fun getLanguages(): AppResult<List<Language>> =
        AppResult.Success(emptyList())
}