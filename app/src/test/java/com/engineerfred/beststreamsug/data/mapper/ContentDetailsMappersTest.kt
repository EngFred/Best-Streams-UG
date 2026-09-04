package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.CastDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailsDto
import com.engineerfred.beststreamsug.data.remote.dto.SeasonDto
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentDetailsMappersTest {
    @Test
    fun mapsMovieDetailsAndNestedMetadata() {
        val details = ContentDetailsDto(
            id = 1608,
            typeId = 1,
            name = "Snake Eyes",
            categoryName = "Adventure, Action",
            languageName = "VJ Junior",
            video320 = "movie-320.mp4",
            trailerType = "server_video",
            cast = listOf(CastDto(id = 434, name = "Actor", type = "Actor")),
        ).toDomain()

        assertEquals(ContentKind.MOVIE, details?.summary?.kind)
        assertEquals(listOf("Adventure", "Action"), details?.categoryNames)
        assertEquals("VJ Junior", details?.languageName)
        assertEquals("Actor", details?.cast?.single()?.name)
        assertEquals(VideoQuality.P320, details?.playback?.sources?.single()?.quality)
        assertEquals("server_video", details?.playback?.trailer?.type)
    }

    @Test
    fun mapsSeriesSeasonsAndSkipsIncompleteNestedRecords() {
        val details = ContentDetailsDto(
            id = 381,
            typeId = 2,
            name = "Series",
            season = listOf(
                SeasonDto(id = 1, name = "Season 1"),
                SeasonDto(id = null, name = "Invalid"),
            ),
        ).toDomain()

        assertEquals(ContentKind.TV_SHOW, details?.summary?.kind)
        assertEquals(1, details?.seasons?.size)
        assertTrue(details?.playback?.sources.isNullOrEmpty())
    }
}
