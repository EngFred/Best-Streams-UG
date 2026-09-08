package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.Banner
import com.engineerfred.beststreamsug.domain.model.BrowseSort
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHomeContentUseCaseTest {
    private val banner = Banner(
        content = ContentSummary(
            id = 1,
            kind = ContentKind.MOVIE,
            title = "Hero",
            thumbnailUrl = null,
            landscapeUrl = null,
            description = null,
            durationMillis = null,
            defaultVideoUrl = null,
            releaseDate = null,
            isPremium = false,
            viewCount = 0,
            likeCount = 0,
            averageRating = 0.0,
            reviewCount = 0,
        ),
        categoryNames = emptyList(),
    )

    @Test
    fun aggregatesSuccessfulHomeSections() = runBlocking {
        val result = createUseCase().invoke()

        assertEquals(1, (result.banners as AppResult.Success).data.size)
        assertEquals("Action", (result.categories as AppResult.Success).data.single().name)
        assertEquals("Luganda", (result.languages as AppResult.Success).data.single().name)
        assertEquals(
            1,
            ((result.categoryRails as AppResult.Success).data.single().content as AppResult.Success)
                .data.items.size,
        )
    }

    @Test
    fun preservesOtherSectionsWhenOneSectionFails() = runBlocking {
        val result = GetHomeContentUseCase(
            bannerRepository = object : BannerRepository {
                override suspend fun getBanners(isHomeScreen: String, typeId: Int) = AppResult.Failure(AppError.NetworkUnavailable)
            },
            metadataRepository = successfulMetadataRepository(),
            browseRepository = successfulBrowseRepository(),
        ).invoke()

        assertTrue(result.banners is AppResult.Failure)
        assertEquals("Action", (result.categories as AppResult.Success).data.single().name)
        assertEquals("Luganda", (result.languages as AppResult.Success).data.single().name)
    }

    private fun createUseCase() = GetHomeContentUseCase(
        bannerRepository = object : BannerRepository {
            override suspend fun getBanners(isHomeScreen: String, typeId: Int) = AppResult.Success(listOf(banner))
        },
        metadataRepository = successfulMetadataRepository(),
        browseRepository = successfulBrowseRepository(),
    )

    private fun successfulBrowseRepository() = object : BrowseRepository {
        override suspend fun getContentByCategory(
            categoryId: Int,
            pageNumber: Int,
            sort: BrowseSort?,
        ) = AppResult.Success(
            Page(
                items = listOf(banner.content),
                currentPage = 1,
                totalPages = 1,
                totalItems = 1,
                hasMore = false,
            ),
        )

        override suspend fun getContentByLanguage(
            languageId: Int,
            pageNumber: Int,
            sort: BrowseSort?,
        ) = AppResult.Success(Page<ContentSummary>(emptyList(), 1, 1, 0, false))
    }

    private fun successfulMetadataRepository() = object : MetadataRepository {
        override suspend fun getCategories() = AppResult.Success(listOf(Category(1, "Action", null, 0)))
        override suspend fun getContentTypes() = AppResult.Success(emptyList<ContentType>())
        override suspend fun getLanguages() = AppResult.Success(listOf(Language(36, "Luganda", null, 0)))
    }
}
