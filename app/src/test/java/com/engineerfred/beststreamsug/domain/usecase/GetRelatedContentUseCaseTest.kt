package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.RelatedContentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRelatedContentUseCaseTest {
    @Test
    fun delegatesRelatedContentRequest() = runBlocking {
        val content = ContentSummary(
            id = 843,
            kind = ContentKind.MOVIE,
            title = "Related",
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
        val repository = object : RelatedContentRepository {
            override suspend fun getRelatedContent(
                contentId: Int,
                typeId: Int,
                videoType: Int,
                pageNumber: Int,
            ) = AppResult.Success(Page(listOf(content), 2, 17, 970, true))
        }

        val result = GetRelatedContentUseCase(repository)(1608, 1, 1, 2)

        assertEquals(2, (result as AppResult.Success).data.currentPage)
        assertEquals(843, result.data.items.single().id)
    }
}
