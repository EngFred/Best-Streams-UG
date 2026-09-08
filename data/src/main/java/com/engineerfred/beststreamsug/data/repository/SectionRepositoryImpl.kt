package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.cache.ContentCatalogCache
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.SectionRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentSection
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class SectionRepositoryImpl @Inject constructor(
    private val remoteDataSource: SectionRemoteDataSource,
    private val metadataRepository: MetadataRepository,
    private val catalogCache: ContentCatalogCache,
) : SectionRepository {
    override suspend fun getSections(isHomeScreen: Int, typeId: Int): AppResult<List<ContentSection>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return when (val result = remoteDataSource.getSections(isHomeScreen, typeId)) {
            is AppResult.Success -> {
                val sections = result.data.mapNotNull { 
                    it.toDomain(vjLookup = { id -> languageMap[id]?.name }) 
                }
                sections.forEach { section ->
                    catalogCache.insertAll(section.items)
                }
                AppResult.Success(sections)
            }
            is AppResult.Failure -> AppResult.Failure(result.error)
        }
    }
}
