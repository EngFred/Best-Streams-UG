package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.BuildConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LiveHomeApiVerificationTest {
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
    fun verifiesHomeBannersEndpoint() = runBlocking {
        val response = api.getBanners(
            com.engineerfred.beststreamsug.data.remote.dto.BannerRequest(
                isHomeScreen = "1",
                typeId = 1,
            ),
        )

        assertEquals(200, response.code())
        assertFalse(response.body()?.result.isNullOrEmpty())
        assertFalse(response.body()?.result?.first()?.name.isNullOrBlank())
    }
}
