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

class ContentDetailsRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: ContentDetailsRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = ContentDetailsRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsDetailIdentifiersAndReturnsFirstResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":1608,"type_id":1,"video_type":1,"name":"Movie"}]}"""),
        )

        val result = dataSource.getContentDetails(1608, 1, 1)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/content_detail", request.path)
        assertEquals("""{"video_id":1608,"type_id":1,"video_type":1}""", request.body.readUtf8())
        assertEquals(1608, (result as AppResult.Success).data.id)
    }

    @Test
    fun mapsEmptyResultToEmptyResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[]}"""),
        )

        val result = dataSource.getContentDetails(1608, 1, 1)

        assertEquals(AppError.EmptyResponse, (result as AppResult.Failure).error)
    }

    @Test
    fun rejectsInvalidIdentifiersWithoutNetworkCall() = runBlocking {
        val result = dataSource.getContentDetails(0, 1, 1)

        assertTrue(result is AppResult.Failure)
        assertTrue(server.requestCount == 0)
    }
}
