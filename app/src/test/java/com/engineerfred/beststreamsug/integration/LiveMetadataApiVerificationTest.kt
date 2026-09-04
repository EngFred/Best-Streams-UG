package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.core.network.ApiConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LiveMetadataApiVerificationTest {
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
    fun verifiesCategoriesEndpoint() = runBlocking {
        val response = api.getCategories(emptyMap())
        assertFalse(response.body()?.result.isNullOrEmpty())
    }

    @Test
    fun verifiesContentTypesEndpoint() = runBlocking {
        val response = api.getTypes(emptyMap())
        assertFalse(response.body()?.result.isNullOrEmpty())
    }

    @Test
    fun verifiesLanguagesEndpoint() = runBlocking {
        val response = api.getLanguages(emptyMap())
        assertFalse(response.body()?.result.isNullOrEmpty())
    }
}
