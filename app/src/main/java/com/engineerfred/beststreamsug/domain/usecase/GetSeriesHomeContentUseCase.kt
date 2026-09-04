package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.SeriesCategoryRail
import com.engineerfred.beststreamsug.domain.model.SeriesHomeContent
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetSeriesHomeContentUseCase @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val browseRepository: BrowseRepository,
) {
    suspend operator fun invoke(): SeriesHomeContent = coroutineScope {
        val banners = async { bannerRepository.getBanners(isHomeScreen = "2", typeId = 2) }
        val rails = SERIES_CATEGORIES.map { category ->
            async { loadSeriesRail(category.id, category.name) }
        }.awaitAll()
        SeriesHomeContent(
            banners = banners.await(),
            categoryRails = rails,
        )
    }

    private suspend fun loadSeriesRail(
        categoryId: Int,
        categoryName: String,
    ): SeriesCategoryRail {
        val series = mutableListOf<ContentSummary>()
        var nextPage = 1
        var hasMore = true
        var firstError: com.engineerfred.beststreamsug.core.common.AppError? = null

        while (hasMore) {
            when (
                val result = browseRepository.getContentByCategory(
                    categoryId = categoryId,
                    pageNumber = nextPage,
                    sort = BrowseSort.Newest,
                )
            ) {
                is AppResult.Success -> {
                    val page = result.data
                    if (page.items.isEmpty()) break
                    series += page.items.filter { it.kind == ContentKind.TV_SHOW }
                    hasMore = page.hasMore && page.currentPage < page.totalPages
                    nextPage = page.currentPage + 1
                }
                is AppResult.Failure -> {
                    firstError = result.error
                    break
                }
            }
        }

        val content = when {
            series.isNotEmpty() -> AppResult.Success(series.distinctBy { it.id })
            firstError != null -> AppResult.Failure(firstError)
            else -> AppResult.Success(emptyList())
        }
        return SeriesCategoryRail(categoryId, categoryName, content)
    }

    private companion object {
        val SERIES_CATEGORIES = listOf(
            SeriesCategory(7, "Action & Adventure"),
            SeriesCategory(29, "American TV Shows"),
            SeriesCategory(23, "Korean Drama"),
            SeriesCategory(25, "Chinese Series"),
            SeriesCategory(31, "British TV Shows"),
        )
    }
}

private data class SeriesCategory(
    val id: Int,
    val name: String,
)
