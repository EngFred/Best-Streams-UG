package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.MetadataRemoteDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MetadataRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: MetadataRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = MetadataRepositoryImpl(
            MetadataRemoteDataSource(
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
    fun mapsRemoteCategoriesToDomainCategories() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":1,"name":"Action","image":"","sort_order":2}]}"""),
        )

        assertEquals(
            AppResult.Success(
                listOf(
                    com.engineerfred.beststreamsug.domain.model.Category(
                        id = 1,
                        name = "Action",
                        imageUrl = null,
                        sortOrder = 2,
                    ),
                ),
            ),
            repository.getCategories(),
        )
    }
}
