package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.google.gson.JsonParser
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: SearchRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = SearchRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsKeywordLanguageAndPage() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(pageBody()))

        val result = dataSource.search("  meg  ", 7, 2)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/search_content", request.path)
        assertEquals(
            JsonParser.parseString("""{"keyword":"meg","language_id":7,"page_no":2}"""),
            JsonParser.parseString(request.body.readUtf8()),
        )
        assertEquals(2, (result as AppResult.Success).data.currentPage)
    }

    @Test
    fun preservesEmptySearchPage() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody(pageBody(result = "[]", totalRows = 0, morePage = false)),
        )

        val result = dataSource.search("missing", pageNumber = 1)

        assertTrue((result as AppResult.Success).data.items.isEmpty())
        assertEquals(0, result.data.totalItems)
    }

    @Test
    fun rejectsBlankKeywordAndInvalidFiltersBeforeRequest() = runBlocking {
        assertTrue(dataSource.search(" ").isFailure())
        assertTrue(dataSource.search("movie", languageId = 0).isFailure())
        assertTrue(dataSource.search("movie", pageNumber = 0).isFailure())
    }

    @Test
    fun rejectsMalformedPagination() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[]}"""),
        )

        val result = dataSource.search("movie")

        assertEquals(
            AppError.InvalidResponse("Invalid pagination metadata"),
            (result as AppResult.Failure).error,
        )
    }

    @Test
    fun mapsHttpFailure() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))

        val result = dataSource.search("movie")

        assertEquals(503, ((result as AppResult.Failure).error as AppError.Http).statusCode)
    }

    private fun pageBody(
        result: String = """[{"id":1608,"type_id":1,"name":"Movie"}]""",
        totalRows: Int = 1,
        morePage: Boolean = true,
    ): String = """
        {
          "status": 200,
          "result": $result,
          "total_rows": $totalRows,
          "total_page": 2,
          "current_page": 2,
          "more_page": $morePage
        }
    """.trimIndent()
}

private fun <T> AppResult<T>.isFailure(): Boolean = this is AppResult.Failure
