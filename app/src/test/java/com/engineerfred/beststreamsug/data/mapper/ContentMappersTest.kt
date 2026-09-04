package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.domain.model.ContentKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentMappersTest {
    @Test
    fun mapsMovieSummaryAndSelectsFirstAvailableVideo() {
        val content = ContentDto(
            id = 1608,
            typeId = 1,
            name = "Snake Eyes",
            thumbnail = "poster.jpg",
            landscape = "backdrop.jpg",
            video320 = "stream.mp4",
            isPremium = 1,
            totalView = 8,
        ).toDomain()

        assertEquals(ContentKind.MOVIE, content?.kind)
        assertEquals("Snake Eyes", content?.title)
        assertEquals("stream.mp4", content?.defaultVideoUrl)
        assertEquals(true, content?.isPremium)
        assertEquals(8, content?.viewCount)
    }

    @Test
    fun mapsTvShowAndRejectsMissingRequiredFields() {
        assertEquals(ContentKind.TV_SHOW, ContentDto(id = 355, typeId = 2, name = "Series").toDomain()?.kind)
        assertNull(ContentDto(id = null, typeId = 1, name = "Missing ID").toDomain())
        assertNull(ContentDto(id = 1, typeId = 1, name = " ").toDomain())
    }
}
