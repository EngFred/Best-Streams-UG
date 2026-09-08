package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EpisodeRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: EpisodeRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = EpisodeRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsShowAndSeasonIdentifiers() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":12714,"show_id":381,"season_id":1,"name":"Episode 1"}]}"""),
        )

        val result = dataSource.getEpisodes(381, 1)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/get_video_by_season_id", request.path)
        assertEquals("""{"show_id":381,"season_id":1}""", request.body.readUtf8())
        assertEquals(12714, (result as AppResult.Success).data.single().id)
    }

    @Test
    fun preservesEmptyEpisodeListAsSuccess() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[]}"""),
        )

        val result = dataSource.getEpisodes(381, 2)

        assertEquals(emptyList<Any>(), (result as AppResult.Success).data)
    }

    @Test
    fun rejectsInvalidIdentifiersWithoutNetworkCall() = runBlocking {
        val result = dataSource.getEpisodes(0, 1)

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.InvalidResponse)
        assertEquals(0, server.requestCount)
    }
}
