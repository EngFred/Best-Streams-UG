package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.EpisodeDto
import com.engineerfred.beststreamsug.domain.model.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpisodeMappersTest {
    @Test
    fun mapsEpisodePlaybackAndMetadata() {
        val episode = EpisodeDto(
            id = 12714,
            showId = 381,
            seasonId = 1,
            name = "Episode 1",
            video320 = "episode.mp4",
            sortOrder = 1,
            isPremium = 1,
        ).toDomain()

        assertEquals("Episode 1", episode?.title)
        assertEquals(VideoQuality.P320, episode?.playback?.sources?.single()?.quality)
        assertEquals(true, episode?.isPremium)
        assertEquals(1, episode?.sortOrder)
    }

    @Test
    fun rejectsIncompleteEpisode() {
        assertNull(EpisodeDto(id = null, showId = 381, seasonId = 1, name = "Episode").toDomain())
        assertNull(EpisodeDto(id = 1, showId = 381, seasonId = 1, name = " ").toDomain())
    }
}
