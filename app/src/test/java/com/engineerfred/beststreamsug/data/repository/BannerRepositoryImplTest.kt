package com.engineerfred.beststreamsug.data.repository

import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import com.engineerfred.beststreamsug.data.remote.datasource.BannerRemoteDataSource
import com.engineerfred.beststreamsug.domain.model.ContentKind
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BannerRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: BannerRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = BannerRepositoryImpl(
            BannerRemoteDataSource(
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
    fun mapsBannerContentToDomain() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":1564,"type_id":1,"name":"Alpha 1","landscape":"backdrop.jpg"}]}"""),
        )

        val result = repository.getBanners(isHomeScreen = "1", typeId = 1)

        assertEquals(ContentKind.MOVIE, (result as com.engineerfred.beststreamsug.core.common.AppResult.Success).data.single().content.kind)
        assertEquals("Alpha 1", result.data.single().content.title)
    }
}
