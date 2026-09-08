package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language

interface MetadataRepository {
    suspend fun getCategories(): AppResult<List<Category>>

    suspend fun getContentTypes(): AppResult<List<ContentType>>

    suspend fun getLanguages(): AppResult<List<Language>>
}
