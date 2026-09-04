package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ApiResponseDto
import com.engineerfred.beststreamsug.data.remote.dto.CategoryDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentTypeDto
import com.engineerfred.beststreamsug.data.remote.dto.LanguageDto
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject

class MetadataRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getCategories(): AppResult<List<CategoryDto>> =
        execute { apiService.getCategories(emptyMap()) }

    suspend fun getContentTypes(): AppResult<List<ContentTypeDto>> =
        execute { apiService.getTypes(emptyMap()) }

    suspend fun getLanguages(): AppResult<List<LanguageDto>> =
        execute { apiService.getLanguages(emptyMap()) }

    private suspend fun <T> execute(
        request: suspend () -> Response<ApiResponseDto<List<T>>>,
    ): AppResult<List<T>> = try {
        val response = request()
        if (!response.isSuccessful) {
            AppResult.Failure(errorMapper.mapHttpStatus(response.code(), response.message()))
        } else {
            val body = response.body()
            when {
                body == null -> AppResult.Failure(AppError.EmptyResponse)
                body.result == null -> AppResult.Failure(
                    AppError.InvalidResponse(body.message),
                )
                else -> AppResult.Success(body.result)
            }
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Throwable) {
        AppResult.Failure(errorMapper.map(exception))
    }
}
