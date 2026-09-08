package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.ContentDetailsRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.repository.ContentDetailsRepository
import javax.inject.Inject

class ContentDetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContentDetailsRemoteDataSource,
) : ContentDetailsRepository {
    override suspend fun getContentDetails(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<ContentDetails> = when (val result = remoteDataSource.getContentDetails(contentId, typeId, videoType)) {
        is AppResult.Success -> result.data.toDomain()?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.InvalidResponse("Content details are missing required fields"))
        is AppResult.Failure -> AppResult.Failure(result.error)
    }
}
