package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.SeriesCategoryRail
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import javax.inject.Inject

class GetSeriesCategoryRailsUseCase @Inject constructor(
    private val browseRepository: BrowseRepository,
) {
    val categoryDefinitions: List<Pair<Int, String>> = listOf(
        7 to "Action & Adventure",
        29 to "American TV Shows",
        23 to "Korean Drama",
        25 to "Chinese Series",
        31 to "British TV Shows",
        24 to "Indian Series",
    )

    suspend fun loadRail(categoryId: Int, categoryName: String): SeriesCategoryRail {
        val items = mutableListOf<com.engineerfred.beststreamsug.domain.model.ContentSummary>()
        var pageNumber = 1
        var hasMore = true
        while (hasMore) {
            when (
                val result = browseRepository.getContentByCategory(
                    categoryId = categoryId,
                    pageNumber = pageNumber,
                    sort = BrowseSort.Newest,
                )
            ) {
                is com.engineerfred.beststreamsug.core.common.AppResult.Success -> {
                    val page = result.data
                    if (page.items.isEmpty()) break
                    items += page.items.filter { it.kind == ContentKind.TV_SHOW }
                    hasMore = page.hasMore && page.currentPage < page.totalPages
                    pageNumber = page.currentPage + 1
                }
                is com.engineerfred.beststreamsug.core.common.AppResult.Failure -> break
            }
        }
        return SeriesCategoryRail(
            categoryId = categoryId,
            categoryName = categoryName,
            content = com.engineerfred.beststreamsug.core.common.AppResult.Success(
                items.distinctBy { it.id },
            ),
        )
    }

    suspend operator fun invoke(): List<SeriesCategoryRail> =
        categoryDefinitions.map { (id, name) -> loadRail(id, name) }
}
