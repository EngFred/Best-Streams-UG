package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.common.Page
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.SearchRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchContentUseCaseTest {
    @Test
    fun delegatesSearchArgumentsAndResult() = runBlocking {
        val expected = AppResult.Success(
            Page(
                items = listOf(
                    ContentSummary(
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
                    ),
                ),
                currentPage = 1,
                totalPages = 1,
                totalItems = 1,
                hasMore = false,
            ),
        )
        var received: Triple<String, Int?, Int>? = null
        val repository = object : SearchRepository {
            override suspend fun search(
                keyword: String,
                languageId: Int?,
                pageNumber: Int,
            ): AppResult<Page<ContentSummary>> {
                received = Triple(keyword, languageId, pageNumber)
                return expected
            }
        }

        val result = SearchContentUseCase(repository)("movie", 7, 2)

        assertEquals(Triple("movie", 7, 2), received)
        assertEquals(expected, result)
    }
}
