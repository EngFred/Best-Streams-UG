package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.HomeCategoryRail
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetHomeCategoryRailsUseCase @Inject constructor(
    private val browseRepository: BrowseRepository,
) {
    suspend operator fun invoke(category: Category): HomeCategoryRail =
        HomeCategoryRail(
            category = category,
            content = browseRepository.getContentByCategory(
                categoryId = category.id,
                pageNumber = 1,
                sort = BrowseSort.Newest,
            ).mapPage<ContentSummary> { it.filter { item -> item.kind == com.engineerfred.beststreamsug.domain.model.ContentKind.MOVIE } },
        )

    suspend operator fun invoke(categories: List<Category>): AppResult<List<HomeCategoryRail>> =
        coroutineScope {
            AppResult.Success(
                categories
                    .sortedBy { it.sortOrder }
                    .take(MAX_HOME_CATEGORY_RAILS)
                    .map { category ->
                        async {
                            invoke(category)
                        }
                    }
                    .awaitAll(),
            )
        }

    private companion object {
        const val MAX_HOME_CATEGORY_RAILS = 8
    }
}

private fun <T> AppResult<Page<T>>.mapPage(
    transform: (List<T>) -> List<T>,
): AppResult<Page<T>> = when (this) {
    is AppResult.Success -> AppResult.Success(
        Page(
            items = transform(data.items),
            currentPage = data.currentPage,
            totalPages = data.totalPages,
            totalItems = data.totalItems,
            hasMore = data.hasMore
        )
    )
    is AppResult.Failure -> this
}
