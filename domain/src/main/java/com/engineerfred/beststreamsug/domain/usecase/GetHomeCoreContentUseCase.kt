package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.HomeContent
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetHomeCoreContentUseCase @Inject constructor(
    private val bannerRepository: BannerRepository,
    private val metadataRepository: MetadataRepository,
    private val sectionRepository: SectionRepository,
) {
    suspend operator fun invoke(): HomeContent = coroutineScope {
        val banners = async { bannerRepository.getBanners(isHomeScreen = "1", typeId = 1) }
        val categories = async { metadataRepository.getCategories() }
        val languages = async { metadataRepository.getLanguages() }
        val sections = async { sectionRepository.getSections(isHomeScreen = 1, typeId = 1) }
        val sectionResult = sections.await()
        HomeContent(
            banners = banners.await(),
            categories = categories.await(),
            languages = languages.await(),
            categoryRails = AppResult.Success(emptyList()),
            sections = sectionResult,
        )
    }
}
