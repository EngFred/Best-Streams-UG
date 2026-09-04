package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ApiResponseDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentBrowseRequest
import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import javax.inject.Inject

class BrowseRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getContentByCategory(
        categoryId: Int,
        pageNumber: Int,
        sort: BrowseSort?,
    ): AppResult<Page<ContentDto>> {
        if (categoryId < 1 || pageNumber < 1) {
            return AppResult.Failure(AppError.InvalidResponse("Category and page must be positive"))
        }
        return execute(
        request = ContentBrowseRequest(
            categoryId = categoryId,
            pageNumber = pageNumber,
        ).withSort(sort),
        call = { apiService.getContentByCategory(it) },
        )
    }

    suspend fun getContentByLanguage(
        languageId: Int,
        pageNumber: Int,
        sort: BrowseSort?,
    ): AppResult<Page<ContentDto>> {
        if (languageId < 1 || pageNumber < 1) {
            return AppResult.Failure(AppError.InvalidResponse("Language and page must be positive"))
        }
        return execute(
        request = ContentBrowseRequest(
            languageId = languageId,
            pageNumber = pageNumber,
        ).withSort(sort),
        call = { apiService.getContentByLanguage(it) },
        )
    }

    private suspend fun execute(
        request: ContentBrowseRequest,
        call: suspend (ContentBrowseRequest) -> Response<ApiResponseDto<List<ContentDto>>>,
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

private fun ContentBrowseRequest.withSort(sort: BrowseSort?): ContentBrowseRequest = when (sort) {
    BrowseSort.Newest -> copy(orderByUpload = "new_to_old")
    BrowseSort.MostViewed -> copy(orderByView = "most_to_least")
    BrowseSort.MostLiked -> copy(orderByLike = "most_to_least")
    null -> this
}
