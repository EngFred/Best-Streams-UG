package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.MetadataRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepositoryImpl @Inject constructor(
    private val remoteDataSource: MetadataRemoteDataSource,
) : MetadataRepository {

    private var languagesCache: List<Language>? = null

    override suspend fun getCategories(): AppResult<List<Category>> =
        remoteDataSource.getCategories().mapItems { it.toDomain() }

    override suspend fun getContentTypes(): AppResult<List<ContentType>> =
        remoteDataSource.getContentTypes().mapItems { it.toDomain() }

    override suspend fun getLanguages(): AppResult<List<Language>> {
        languagesCache?.let { return AppResult.Success(it) }
        val result = remoteDataSource.getLanguages().mapItems { it.toDomain() }
        if (result is AppResult.Success) {
            languagesCache = result.data
        }
        return result
    }
}

private inline fun <T, R> AppResult<List<T>>.mapItems(
    transform: (T) -> R?,
): AppResult<List<R>> = when (this) {
    is AppResult.Success -> AppResult.Success(data.mapNotNull(transform))
    is AppResult.Failure -> this
}
