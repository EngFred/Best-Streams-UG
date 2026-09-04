package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.EpisodeDto
import com.engineerfred.beststreamsug.data.remote.dto.EpisodeRequest
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class EpisodeRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getEpisodes(showId: Int, seasonId: Int): AppResult<List<EpisodeDto>> {
        if (showId < 1 || seasonId < 1) {
            return AppResult.Failure(AppError.InvalidResponse("Show and season identifiers must be positive"))
        }
        return try {
            val response = apiService.getEpisodes(EpisodeRequest(showId, seasonId))
            if (!response.isSuccessful) {
                AppResult.Failure(errorMapper.mapHttpStatus(response.code(), response.message()))
            } else {
                val result = response.body()?.result
                when {
                    result == null -> AppResult.Failure(AppError.EmptyResponse)
                    else -> AppResult.Success(result)
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            AppResult.Failure(errorMapper.map(exception))
        }
    }
}
