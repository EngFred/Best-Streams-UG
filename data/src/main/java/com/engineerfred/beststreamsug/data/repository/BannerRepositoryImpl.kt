package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.cache.ContentCatalogCache
import com.engineerfred.beststreamsug.data.mapper.toBanner
import com.engineerfred.beststreamsug.data.mapper.resolveVjName
import com.engineerfred.beststreamsug.data.remote.datasource.BannerRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class BannerRepositoryImpl @Inject constructor(
    private val remoteDataSource: BannerRemoteDataSource,
    private val metadataRepository: MetadataRepository,
    private val catalogCache: ContentCatalogCache,
) : BannerRepository {
    override suspend fun getBanners(isHomeScreen: String, typeId: Int): AppResult<List<Banner>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return when (val result = remoteDataSource.getBanners(isHomeScreen, typeId)) {
            is AppResult.Success -> {
                val banners = result.data.mapNotNull { 
                    it.toBanner(it.languageId.resolveVjName { id -> languageMap[id]?.name }) 
                }
                catalogCache.insertAll(banners.map { it.content })
                AppResult.Success(banners)
            }
            is AppResult.Failure -> AppResult.Failure(result.error)
        }
    }
}
