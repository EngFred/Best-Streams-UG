package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.createTestApiService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class BannerRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: BannerRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = BannerRemoteDataSource(
            createTestApiService(server.url("/").toString()),
            NetworkErrorMapper(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsHomeBannerRequestAndParsesContent() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"status":200,"result":[{"id":1564,"type_id":1,"name":"Alpha 1","landscape":"backdrop.jpg"}]}"""),
        )

        val result = dataSource.getBanners(isHomeScreen = "1", typeId = 1)
        val request = server.takeRequest(1, TimeUnit.SECONDS) ?: error("Expected request")

        assertEquals("/get_banner", request.path)
        assertEquals("""{"is_home_screen":"1","type_id":1}""", request.body.readUtf8())
        assertEquals(1564, (result as AppResult.Success).data.single().id)
        assertEquals("backdrop.jpg", result.data.single().landscape)
    }

    @Test
    fun mapsEmptyResponseBodyAsFailure() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        val result = dataSource.getBanners(isHomeScreen = "1", typeId = 1)

        assertNotNull((result as AppResult.Failure).error)
    }
}
