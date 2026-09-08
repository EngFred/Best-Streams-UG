package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.BrowseRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentKind
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BrowseRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: BrowseRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = BrowseRepositoryImpl(
            BrowseRemoteDataSource(
                createTestApiService(server.url("/").toString()),
                NetworkErrorMapper(),
            ),
            emptyMetadataRepository(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun mapsPageItemsToDomainSummaries() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"status":200,"result":[{"id":1608,"type_id":1,"name":"Movie","video_320":"stream.mp4"}],"total_rows":1,"total_page":1,"current_page":1,"more_page":false}""",
            ),
        )

        val result = repository.getContentByCategory(1, 1)

        assertEquals(ContentKind.MOVIE, (result as com.engineerfred.beststreamsug.core.common.AppResult.Success).data.items.single().kind)
        assertEquals("stream.mp4", result.data.items.single().defaultVideoUrl)
    }
}
