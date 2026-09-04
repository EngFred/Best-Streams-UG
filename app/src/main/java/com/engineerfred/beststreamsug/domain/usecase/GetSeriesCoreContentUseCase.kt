package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.SeriesHomeContent
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetSeriesCoreContentUseCase @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val sectionRepository: SectionRepository,
) {
    suspend operator fun invoke(): SeriesHomeContent = coroutineScope {
        val banners = async { bannerRepository.getBanners(isHomeScreen = "2", typeId = 2) }
        val sections = async { sectionRepository.getSections(isHomeScreen = 1, typeId = 1) }
        SeriesHomeContent(
            banners = banners.await(),
            sections = sections.await().extractSeriesSections(),
            categoryRails = emptyList(),
        )
    }
}

private fun AppResult<List<com.engineerfred.beststreamsug.domain.model.ContentSection>>.extractSeriesSections():
    AppResult<List<com.engineerfred.beststreamsug.domain.model.ContentSection>> = when (this) {
    is AppResult.Success -> AppResult.Success(
        data.filter { 
            it.title.contains("Recently added Series", ignoreCase = true) ||
            it.title.contains("Most Viewed Series", ignoreCase = true)
        }.map { section ->
            section.copy(items = section.items.filter { it.kind == ContentKind.TV_SHOW })
        }
    )
    is AppResult.Failure -> AppResult.Failure(error)
}
