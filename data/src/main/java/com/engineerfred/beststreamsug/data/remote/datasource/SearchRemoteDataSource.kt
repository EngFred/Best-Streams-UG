package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.data.remote.dto.SearchContentRequest
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun search(
        keyword: String,
        languageId: Int? = null,
        pageNumber: Int = 1,
    ): AppResult<Page<ContentDto>> {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank() || pageNumber < 1 || languageId?.let { it < 1 } == true) {
            return AppResult.Failure(AppError.InvalidResponse("Search keyword, language, and page must be valid"))
        }
        return try {
            val response = apiService.searchContent(
                SearchContentRequest(normalizedKeyword, languageId, pageNumber),
            )
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
}
