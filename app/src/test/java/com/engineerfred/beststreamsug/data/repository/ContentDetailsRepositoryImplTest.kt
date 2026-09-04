package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.ContentDetailsRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentKind
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ContentDetailsRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ContentDetailsRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = ContentDetailsRepositoryImpl(
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
    fun mapsDetailsToDomain() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":381,"type_id":2,"name":"Series","season":[{"id":1,"name":"Season 1"}]}]}"""),
        )

        val result = repository.getContentDetails(381, 2, 2)

        assertEquals(ContentKind.TV_SHOW, (result as AppResult.Success).data.summary.kind)
        assertEquals(1, result.data.seasons.single().id)
    }
}
