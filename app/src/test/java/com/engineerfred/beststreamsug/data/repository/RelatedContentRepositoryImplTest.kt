package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.RelatedContentRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentKind
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RelatedContentRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RelatedContentRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = RelatedContentRepositoryImpl(
            RelatedContentRemoteDataSource(
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
    fun mapsRelatedContentToDomainPage() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":843,"type_id":1,"name":"Related"}],"total_rows":1,"total_page":1,"current_page":1,"more_page":false}"""),
        )

        val result = repository.getRelatedContent(1608, 1, 1, 1)

        assertEquals(ContentKind.MOVIE, (result as AppResult.Success).data.items.single().kind)
        assertEquals("Related", result.data.items.single().title)
    }
}
