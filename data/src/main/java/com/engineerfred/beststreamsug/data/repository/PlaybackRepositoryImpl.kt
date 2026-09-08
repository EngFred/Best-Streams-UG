package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.remote.datasource.ContentDetailsRemoteDataSource
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.repository.PlaybackRepository
import javax.inject.Inject

class PlaybackRepositoryImpl @Inject constructor(
    private val contentDetailsRemoteDataSource: ContentDetailsRemoteDataSource,
) : PlaybackRepository {
    override suspend fun getContentPlayback(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<PlaybackMetadata> = when (
        val result = contentDetailsRemoteDataSource.getContentDetails(contentId, typeId, videoType)
    ) {
        is AppResult.Success -> result.data.toDomain()?.let { AppResult.Success(it.playback) }
            ?: AppResult.Failure(
                com.engineerfred.beststreamsug.core.common.AppError.InvalidResponse(
                    "Playback metadata is missing required content fields",
                ),
            )
        is AppResult.Failure -> AppResult.Failure(result.error)
    }
}
