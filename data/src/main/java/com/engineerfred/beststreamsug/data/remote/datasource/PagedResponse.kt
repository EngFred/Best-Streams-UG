package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.data.remote.dto.ApiResponseDto

internal fun <T> ApiResponseDto<List<T>>.toPageResult(): AppResult<Page<T>> {
    if (status != 200) {
        return AppResult.Failure(AppError.InvalidResponse(message ?: "API request was not successful"))
    }
    val pageItems = result ?: return AppResult.Failure(AppError.InvalidResponse(message))
    val page = current_page
    val pages = total_page
    val items = total_rows
    val more = more_page
    if (page == null || pages == null || items == null || more == null ||
        page < 1 || pages < 1 || page > pages || items < 0
    ) {
        return AppResult.Failure(AppError.InvalidResponse("Invalid pagination metadata"))
    }
    return AppResult.Success(
        Page(
            items = pageItems,
            currentPage = page,
            totalPages = pages,
            totalItems = items,
            hasMore = more && pageItems.isNotEmpty() && page < pages,
        ),
    )
}
