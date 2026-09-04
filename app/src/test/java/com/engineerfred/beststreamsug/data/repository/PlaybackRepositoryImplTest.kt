package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.ContentDetailsRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.VideoQuality
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlaybackRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: PlaybackRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = PlaybackRepositoryImpl(
            ContentDetailsRemoteDataSource(
                createTestApiService(server.url("/").toString()),
                NetworkErrorMapper(),
            ),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun exposesVerifiedDirectSourcesFromContentDetails() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":1608,"type_id":1,"name":"Movie","video_320":"movie-320.mp4","video_720":"movie-720.mp4"}]}"""),
        )

        val result = repository.getContentPlayback(1608, 1, 1)

        assertEquals(2, (result as AppResult.Success).data.sources.size)
        assertEquals(VideoQuality.P720, result.data.sources.last().quality)
    }
}
