package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.BuildConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.RelatedContentRequest
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LiveRelatedContentApiVerificationTest {
    private lateinit var api: AppApiService

    @Before
    fun setUp() {
        assumeTrue(System.getProperty("runLiveApiTests") == "true")
        api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(AppApiService::class.java)
    }

    @Test
    fun verifiesMisspelledRelatedEndpointAndPagination() = runBlocking {
        val response = api.getRelatedContent(RelatedContentRequest(1608, 1, 1, 2))
        val body = response.body()

        assertEquals(200, response.code())
        assertFalse(body?.result.isNullOrEmpty())
        assertEquals(2, body?.current_page)
        assertEquals(17, body?.total_page)
    }
}
