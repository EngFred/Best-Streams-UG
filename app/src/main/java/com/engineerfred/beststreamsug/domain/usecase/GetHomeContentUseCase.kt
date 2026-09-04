package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.HomeContent
import com.engineerfred.beststreamsug.domain.model.HomeCategoryRail
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetHomeContentUseCase @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val metadataRepository: MetadataRepository,
    private val browseRepository: BrowseRepository,
) {
    suspend operator fun invoke(): HomeContent = coroutineScope {
        val banners = async { bannerRepository.getBanners(isHomeScreen = "1", typeId = 1) }
        val categories = async { metadataRepository.getCategories() }
        val languages = async { metadataRepository.getLanguages() }
        val categoryResult = categories.await()
        val categoryRails = when (categoryResult) {
            is AppResult.Success -> {
                val railJobs = categoryResult.data
                    .sortedBy { it.sortOrder }
                    .take(MAX_HOME_CATEGORY_RAILS)
                    .map { category ->
                        async {
                            HomeCategoryRail(
                                category = category,
                                content = browseRepository.getContentByCategory(
                                    categoryId = category.id,
                                    pageNumber = 1,
                                    sort = BrowseSort.Newest,
                                ),
                            )
                        }
                    }
                AppResult.Success(railJobs.awaitAll())
            }
            is AppResult.Failure -> AppResult.Failure(categoryResult.error)
        }

        HomeContent(
            banners = banners.await(),
            categories = categoryResult,
            languages = languages.await(),
            categoryRails = categoryRails,
        )
    }

    private companion object {
        const val MAX_HOME_CATEGORY_RAILS = 8
    }
}
