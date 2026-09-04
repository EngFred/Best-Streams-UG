package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BrowseUseCasesTest {
    private val content = ContentSummary(
        id = 1,
        kind = ContentKind.MOVIE,
        title = "Movie",
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
    )
    private val page = Page(listOf(content), 1, 1, 1, false)
    private val repository = object : BrowseRepository {
        override suspend fun getContentByCategory(categoryId: Int, pageNumber: Int, sort: com.engineerfred.beststreamsug.domain.model.BrowseSort?) =
            AppResult.Success(page)

        override suspend fun getContentByLanguage(languageId: Int, pageNumber: Int, sort: com.engineerfred.beststreamsug.domain.model.BrowseSort?) =
            AppResult.Success(page)
    }

    @Test
    fun categoryUseCaseDelegatesParameters() = runBlocking {
        assertEquals(1, (GetCategoryContentUseCase(repository)(1, 1) as AppResult.Success).data.items.single().id)
    }

    @Test
    fun languageUseCaseDelegatesParameters() = runBlocking {
        assertEquals(1, (GetLanguageContentUseCase(repository)(36, 1) as AppResult.Success).data.items.single().id)
    }
}
