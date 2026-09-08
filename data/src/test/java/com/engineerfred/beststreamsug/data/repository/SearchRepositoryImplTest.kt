package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.SearchRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentKind
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = SearchRepositoryImpl(
            SearchRemoteDataSource(
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
    fun mapsSearchResultsToDomain() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(
                """{"status":200,"result":[{"id":1608,"type_id":1,"name":"Movie"}],"total_rows":1,"total_page":1,"current_page":1,"more_page":false}""",
            ),
        )

        val result = repository.search("movie")

        assertEquals(
            ContentKind.MOVIE,
            (result as AppResult.Success).data.items.single().kind,
        )
    }
}
