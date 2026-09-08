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

class BrowseRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: BrowseRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = BrowseRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsCategoryAndSortAndMapsFirstPage() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(pageBody(1, 15, 855, true)))

        val result = dataSource.getContentByCategory(1, 1, com.engineerfred.beststreamsug.domain.model.BrowseSort.Newest)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/content_by_category", request.path)
        assertEquals(
            JsonParser.parseString("""{"category_id":1,"page_no":1,"order_by_upload":"new_to_old"}"""),
            JsonParser.parseString(request.body.readUtf8()),
        )
        assertEquals(1, (result as AppResult.Success).data.currentPage)
        assertEquals(15, result.data.totalPages)
        assertTrue(result.data.hasMore)
    }

    @Test
    fun mapsSubsequentAndFinalPages() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(pageBody(2, 15, 855, true)))
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(pageBody(15, 15, 855, false)))

        val second = dataSource.getContentByCategory(1, 2, null)
        val final = dataSource.getContentByCategory(1, 15, null)

        assertEquals(2, (second as AppResult.Success).data.currentPage)
        assertEquals(false, (final as AppResult.Success).data.hasMore)
    }

    @Test
    fun preservesEmptyPageAsSuccessfulResult() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(pageBody(1, 1, 0, false, empty = true)))

        val result = dataSource.getContentByLanguage(36, 1, null)

        assertEquals(emptyList<Any>(), (result as AppResult.Success).data.items)
        assertEquals(0, result.data.totalItems)
    }

    @Test
    fun mapsServerError() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = dataSource.getContentByLanguage(36, 1, null)

        assertEquals(500, (result as AppResult.Failure).error.let { it as AppError.Http }.statusCode)
    }

    @Test
    fun rejectsMalformedPaginationMetadata() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[]}"""),
        )

        val result = dataSource.getContentByCategory(1, 1, null)

        assertEquals(AppError.InvalidResponse("Invalid pagination metadata"), (result as AppResult.Failure).error)
    }

    @Test
    fun rejectsNonPositiveRequestedPageBeforeMakingRequest() = runBlocking {
        val result = dataSource.getContentByCategory(1, 0, null)

        assertTrue(result is AppResult.Failure)
    }

    private fun pageBody(
        currentPage: Int,
        totalPage: Int,
        totalRows: Int,
        morePage: Boolean,
        empty: Boolean = false,
    ): String = """
        {
          "status": 200,
          "result": ${if (empty) "[]" else """[{"id":1608,"type_id":1,"name":"Movie"}]"""},
          "total_rows": $totalRows,
          "total_page": $totalPage,
          "current_page": $currentPage,
          "more_page": $morePage
        }
    """.trimIndent()
}
