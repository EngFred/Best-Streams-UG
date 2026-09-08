package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.EpisodeRemoteDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EpisodeRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: EpisodeRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = EpisodeRepositoryImpl(
            EpisodeRemoteDataSource(
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
    fun sortsEpisodesBySortOrder() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """{"status":200,"result":[{"id":2,"show_id":381,"season_id":1,"name":"Episode 2","sort_order":2},{"id":1,"show_id":381,"season_id":1,"name":"Episode 1","sort_order":1}]}""",
                ),
        )

        val result = repository.getEpisodes(381, 1)

        assertEquals(1, (result as AppResult.Success).data.first().sortOrder)
        assertEquals(2, result.data.last().sortOrder)
    }
}
