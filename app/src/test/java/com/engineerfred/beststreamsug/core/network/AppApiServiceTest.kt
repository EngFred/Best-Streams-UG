package com.engineerfred.beststreamsug.data.remote.api

import com.google.gson.JsonParser
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AppApiServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var api: AppApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = createTestApiService(server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun serializesRequestAndDeserializesResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "status": 1,
                      "message": "Record found.",
                      "result": [{"id": 1, "name": "Action"}],
                      "total_rows": 1,
                      "total_page": 1,
                      "current_page": 1,
                      "more_page": false
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.getCategories(mapOf("page_no" to 1))
        val request = server.takeRequest(1, TimeUnit.SECONDS)
            ?: error("Expected a request to be recorded")

        assertEquals("POST", request.method)
        assertEquals("/get_category", request.path)
        assertEquals(
            JsonParser.parseString("""{"page_no":1}"""),
            JsonParser.parseString(request.body.readUtf8()),
        )
        assertEquals(200, response.code())
        assertEquals(1, response.body()?.status)
        assertEquals("Record found.", response.body()?.message)
        assertEquals(1, response.body()?.result?.size)
        assertEquals(false, response.body()?.more_page)
    }

    @Test
    fun exposesHttpErrorResponseWithoutParsingAsSuccess() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":0,"message":"Server error"}"""),
        )

        val response = api.getTypes(emptyMap())

        assertEquals(500, response.code())
        assertEquals(false, response.isSuccessful)
    }

    @Test
    fun handlesMalformedJsonAsSerializationFailure() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":1,"result":"""),
        )

        val exception = runCatching { api.getLanguages(emptyMap()) }.exceptionOrNull()

        assertNotNull(exception)
    }
}
