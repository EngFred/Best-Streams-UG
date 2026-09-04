package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.BrowseRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class BrowseRepositoryImpl @Inject constructor(
    private val remoteDataSource: BrowseRemoteDataSource,
    private val metadataRepository: MetadataRepository,
) : BrowseRepository {
    override suspend fun getContentByCategory(
        categoryId: Int,
        pageNumber: Int,
        sort: BrowseSort?,
    ): AppResult<Page<ContentSummary>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()
        
        return remoteDataSource
            .getContentByCategory(categoryId, pageNumber, sort)
            .mapPageItems { 
                val vjName = it.languageId?.toIntOrNull()?.let { id -> languageMap[id]?.name }
                it.toDomain(vjName) 
            }
    }

    override suspend fun getContentByLanguage(
        languageId: Int,
        pageNumber: Int,
        sort: BrowseSort?,
    ): AppResult<Page<ContentSummary>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return remoteDataSource
            .getContentByLanguage(languageId, pageNumber, sort)
            .mapPageItems { 
                val vjName = it.languageId?.toIntOrNull()?.let { id -> languageMap[id]?.name }
                it.toDomain(vjName) 
            }
    }
}

private inline fun <T, R> AppResult<Page<T>>.mapPageItems(
    transform: (T) -> R?,
): AppResult<Page<R>> = when (this) {
    is AppResult.Success -> AppResult.Success(
        Page(
            items = data.items.mapNotNull(transform),
            currentPage = data.currentPage,
            totalPages = data.totalPages,
            totalItems = data.totalItems,
            hasMore = data.hasMore,
        ),
    )
    is AppResult.Failure -> AppResult.Failure(error)
}
