package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.data.cache.ContentCatalogCache
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.mapper.resolveVjName
import com.engineerfred.beststreamsug.data.remote.datasource.SearchRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.SearchRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: SearchRemoteDataSource,
    private val metadataRepository: MetadataRepository,
    private val catalogCache: ContentCatalogCache,
) : SearchRepository {
    override suspend fun search(
        keyword: String,
        languageId: Int?,
        pageNumber: Int,
    ): AppResult<Page<ContentSummary>> {
        // NOTE: Remote search API endpoint is currently disabled/down.
        // Focusing purely on local client-side search across indexed catalog items for now.
        /*
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return when (
            val result = remoteDataSource.search(keyword, languageId, pageNumber)
        ) {
            is AppResult.Success -> {
                val domainItems = result.data.items.mapNotNull { 
                    it.toDomain(it.languageId.resolveVjName { id -> languageMap[id]?.name }) 
                }
                catalogCache.insertAll(domainItems)
                AppResult.Success(
                    Page(
                        items = domainItems,
                        currentPage = result.data.currentPage,
                        totalPages = result.data.totalPages,
                        totalItems = result.data.totalItems,
                        hasMore = result.data.hasMore,
                    ),
                )
            }
            is AppResult.Failure -> {
                val localMatches = catalogCache.search(keyword)
                if (localMatches.isNotEmpty()) {
                    AppResult.Success(
                        Page(
                            items = localMatches,
                            currentPage = 1,
                            totalPages = 1,
                            totalItems = localMatches.size,
                            hasMore = false,
                        ),
                    )
                } else {
                    AppResult.Failure(result.error)
                }
            }
        }
        */

        val localMatches = catalogCache.search(keyword)
        return AppResult.Success(
            Page(
                items = localMatches,
                currentPage = 1,
                totalPages = 1,
                totalItems = localMatches.size,
                hasMore = false,
            ),
        )
    }
}
