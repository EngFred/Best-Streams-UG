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

class RelatedContentRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: RelatedContentRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = RelatedContentRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun preservesExactMisspelledRouteAndPaginationRequest() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":843,"type_id":1,"name":"Related"}],"total_rows":970,"total_page":17,"current_page":2,"more_page":true}"""),
        )

        val result = dataSource.getRelatedContent(1608, 1, 1, 2)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/get_releted_content", request.path)
        assertEquals("""{"video_id":1608,"type_id":1,"video_type":1,"page_no":2}""", request.body.readUtf8())
        assertEquals(2, (result as AppResult.Success).data.currentPage)
        assertEquals(17, result.data.totalPages)
        assertTrue(result.data.hasMore)
    }

    @Test
    fun rejectsMalformedPaginationMetadata() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[]}"""),
        )

        val result = dataSource.getRelatedContent(1608, 1, 1, 1)

        assertEquals(
            AppError.InvalidResponse("Invalid pagination metadata"),
            (result as AppResult.Failure).error,
        )
    }
}
