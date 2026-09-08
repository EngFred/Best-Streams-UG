package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.repository.ContentDetailsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetContentDetailsUseCaseTest {
    @Test
    fun delegatesDetailRequestToRepository() = runBlocking {
        val details = ContentDetails(
            summary = ContentSummary(
                id = 1608,
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
            categoryNames = emptyList(),
            languageName = null,
            cast = emptyList(),
            playback = PlaybackMetadata(emptyList(), null, emptyList()),
            seasons = emptyList(),
            isUserLike = false,
            commentCount = 0,
        )
        val repository = object : ContentDetailsRepository {
            override suspend fun getContentDetails(contentId: Int, typeId: Int, videoType: Int) =
                AppResult.Success(details)
        }

        val result = GetContentDetailsUseCase(repository)(1608, 1, 1)

        assertEquals(1608, (result as AppResult.Success).data.summary.id)
    }
}
