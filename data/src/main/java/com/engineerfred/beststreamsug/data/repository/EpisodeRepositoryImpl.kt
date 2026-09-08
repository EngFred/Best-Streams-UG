package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.data.mapper.toDomain
import com.engineerfred.beststreamsug.data.remote.datasource.EpisodeRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.repository.EpisodeRepository
import javax.inject.Inject

class EpisodeRepositoryImpl @Inject constructor(
    private val remoteDataSource: EpisodeRemoteDataSource,
) : EpisodeRepository {
    override suspend fun getEpisodes(showId: Int, seasonId: Int): AppResult<List<Episode>> =
        when (val result = remoteDataSource.getEpisodes(showId, seasonId)) {
            is AppResult.Success -> AppResult.Success(
                result.data.mapNotNull { it.toDomain() }.sortedBy { it.sortOrder },
            )
            is AppResult.Failure -> AppResult.Failure(result.error)
        }
}
