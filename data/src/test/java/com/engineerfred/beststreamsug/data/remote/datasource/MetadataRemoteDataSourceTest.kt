package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MetadataRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: MetadataRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = MetadataRemoteDataSource(
            apiService = createTestApiService(server.url("/").toString()),
            errorMapper = NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun mapsCategoryResponseToSuccessfulResult() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"message":"ok","result":[{"id":1,"name":"Action"}]}"""),
        )

        val result = dataSource.getCategories()

        assertEquals(
            AppResult.Success(
                listOf(
                    com.engineerfred.beststreamsug.data.remote.dto.CategoryDto(
                        id = 1,
                        name = "Action",
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun mapsHttpFailureToApplicationError() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = dataSource.getLanguages()

        assertTrue(result is AppResult.Failure)
        assertEquals(500, (result as AppResult.Failure).error.let { it as AppError.Http }.statusCode)
    }

    @Test
    fun mapsMissingResultToInvalidResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"message":"missing"}"""),
        )

        val result = dataSource.getContentTypes()

        assertEquals(
            AppResult.Failure(AppError.InvalidResponse("missing")),
            result,
        )
    }
}
