package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.mapper.toBanner
import com.engineerfred.beststreamsug.data.remote.datasource.BannerRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import javax.inject.Inject

class BannerRepositoryImpl @Inject constructor(
    private val remoteDataSource: BannerRemoteDataSource,
    private val metadataRepository: MetadataRepository,
) : BannerRepository {
    override suspend fun getBanners(isHomeScreen: String, typeId: Int): AppResult<List<Banner>> {
        val languages = metadataRepository.getLanguages()
        val languageMap = (languages as? AppResult.Success)?.data?.associateBy { it.id } ?: emptyMap()

        return when (val result = remoteDataSource.getBanners(isHomeScreen, typeId)) {
            is AppResult.Success -> AppResult.Success(result.data.mapNotNull { 
                val vjName = it.languageId?.toIntOrNull()?.let { id -> languageMap[id]?.name }
                it.toBanner(vjName) 
            })
            is AppResult.Failure -> AppResult.Failure(result.error)
        }
    }
}
