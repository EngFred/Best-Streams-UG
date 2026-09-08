package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.repository.EpisodeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEpisodesUseCaseTest {
    @Test
    fun delegatesToEpisodeRepository() = runBlocking {
        val episode = Episode(
            id = 12714,
            showId = 381,
            seasonId = 1,
            title = "Episode 1",
            thumbnailUrl = null,
            landscapeUrl = null,
            description = null,
            durationMillis = null,
            playback = PlaybackMetadata(emptyList(), null, emptyList()),
            isPremium = true,
            viewCount = 0,
            sortOrder = 1,
        )
        val repository = object : EpisodeRepository {
            override suspend fun getEpisodes(showId: Int, seasonId: Int) =
                AppResult.Success(listOf(episode))
        }

        val result = GetEpisodesUseCase(repository)(381, 1)

        assertEquals(12714, (result as AppResult.Success).data.single().id)
    }
}
