package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ApiResponseDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.data.remote.dto.RelatedContentRequest
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject

class RelatedContentRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getRelatedContent(
        contentId: Int,
        typeId: Int,
        videoType: Int,
        pageNumber: Int,
    ): AppResult<Page<ContentDto>> {
        if (contentId < 1 || typeId < 1 || videoType < 1 || pageNumber < 1) {
            return AppResult.Failure(AppError.InvalidResponse("Content identifiers and page must be positive"))
        }
        return execute(
            request = RelatedContentRequest(contentId, typeId, videoType, pageNumber),
            call = { apiService.getRelatedContent(it) },
        )
    }

    private suspend fun execute(
        request: RelatedContentRequest,
        call: suspend (RelatedContentRequest) -> Response<ApiResponseDto<List<ContentDto>>>,
    ): AppResult<Page<ContentDto>> = try {
        val response = call(request)
        if (!response.isSuccessful) {
            AppResult.Failure(errorMapper.mapHttpStatus(response.code(), response.message()))
        } else {
            response.body()?.toPageResult() ?: AppResult.Failure(AppError.EmptyResponse)
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Throwable) {
        AppResult.Failure(errorMapper.map(exception))
    }
}
