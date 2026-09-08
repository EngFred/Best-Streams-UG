package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.BuildConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailRequest
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LivePlaybackApiVerificationTest {
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
    fun verifiesDirectPlaybackSourceReturnedByDetails() = runBlocking {
        val details = api.getContentDetails(ContentDetailRequest(1608, 1, 1))
        val url = details.body()?.result?.first()?.video320.orEmpty()

        assertFalse(url.isBlank())
        OkHttpClient().newCall(Request.Builder().url(url).head().build()).execute().use {
            assertTrue(it.isSuccessful)
        }
    }

    @Test
    fun verifiesServerVideoRouteIsUnavailable() = runBlocking {
        val request = Request.Builder()
            .url("${BuildConfig.API_BASE_URL}server_video")
            .post(okhttp3.RequestBody.create(null, """{"video_id":1608,"type_id":1,"video_type":1}"""))
            .build()

        OkHttpClient().newCall(request).execute().use {
            assertTrue(it.code == 404)
        }
    }
}
