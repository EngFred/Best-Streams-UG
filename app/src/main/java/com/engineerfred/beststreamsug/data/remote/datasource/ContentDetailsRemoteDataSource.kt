package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailRequest
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailsDto
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ContentDetailsRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getContentDetails(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<ContentDetailsDto> {
        if (contentId < 1 || typeId < 1 || videoType < 1) {
            return AppResult.Failure(AppError.InvalidResponse("Content identifiers must be positive"))
        }
        return try {
            val response = apiService.getContentDetails(
                ContentDetailRequest(contentId, typeId, videoType),
            )
            if (!response.isSuccessful) {
                AppResult.Failure(errorMapper.mapHttpStatus(response.code(), response.message()))
            } else {
                val result = response.body()?.result
                when {
                    result == null -> AppResult.Failure(AppError.EmptyResponse)
                    result.isEmpty() -> AppResult.Failure(AppError.EmptyResponse)
                    else -> AppResult.Success(result.first())
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            AppResult.Failure(errorMapper.map(exception))
        }
    }
}
