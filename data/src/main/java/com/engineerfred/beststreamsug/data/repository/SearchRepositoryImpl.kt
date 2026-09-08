package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.SearchRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.SearchRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: SearchRemoteDataSource,
    private val metadataRepository: MetadataRepository,
) : SearchRepository {
    override suspend fun search(
        keyword: String,
        languageId: Int?,
        pageNumber: Int,
    ): AppResult<Page<ContentSummary>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return when (
            val result = remoteDataSource.search(keyword, languageId, pageNumber)
        ) {
            is AppResult.Success -> AppResult.Success(
                Page(
                    items = result.data.items.mapNotNull { 
                        val vjName = it.languageId?.toIntOrNull()?.let { id -> languageMap[id]?.name }
                        it.toDomain(vjName) 
                    },
                    currentPage = result.data.currentPage,
                    totalPages = result.data.totalPages,
                    totalItems = result.data.totalItems,
                    hasMore = result.data.hasMore,
                ),
            )
            is AppResult.Failure -> AppResult.Failure(result.error)
        }
    }
}
