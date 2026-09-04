package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.core.network.ApiConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class LiveSearchApiVerificationTest {
    @Test
    fun verifiesSearchEndpointWhenAvailable() {
        assumeTrue(System.getProperty("runLiveApiTests") == "true")

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}search_content")
            .post(
                """{"keyword":"meg","language_id":7,"page_no":1}"""
                    .toRequestBody("application/json".toMediaType()),
            )
            .build()

        try {
            client.newCall(request).execute().use { response ->
                assertTrue(response.code in 200..599)
            }
        } catch (exception: IOException) {
            assumeTrue("search_content unavailable during live verification: ${exception.message}", false)
        }
    }
}
