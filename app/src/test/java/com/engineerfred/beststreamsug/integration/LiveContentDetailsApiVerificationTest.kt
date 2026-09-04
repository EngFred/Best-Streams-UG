package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.core.network.ApiConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailRequest
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LiveContentDetailsApiVerificationTest {
    private lateinit var api: AppApiService

    @Before
    fun setUp() {
        assumeTrue(System.getProperty("runLiveApiTests") == "true")
        api = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(AppApiService::class.java)
    }

    @Test
    fun verifiesMovieDetails() = runBlocking {
        val response = api.getContentDetails(ContentDetailRequest(1608, 1, 1))

        assertEquals(200, response.code())
        assertFalse(response.body()?.result.isNullOrEmpty())
        assertEquals(1608, response.body()?.result?.first()?.id)
        assertTrue(response.body()?.result?.first()?.cast.orEmpty().isNotEmpty())
    }

    @Test
    fun verifiesTvDetailsAndSeasons() = runBlocking {
        val response = api.getContentDetails(ContentDetailRequest(381, 2, 2))

        assertEquals(200, response.code())
        assertEquals(381, response.body()?.result?.first()?.id)
        assertTrue(response.body()?.result?.first()?.season.orEmpty().isNotEmpty())
    }
}
