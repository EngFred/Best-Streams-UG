package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.core.network.ApiConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
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

class LiveBrowseApiVerificationTest {
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
    fun verifiesCategoryPaginationAndContentShape() = runBlocking {
        val response = api.getContentByCategory(
            com.engineerfred.beststreamsug.data.remote.dto.ContentBrowseRequest(
                categoryId = 1,
                pageNumber = 1,
            ),
        )

        assertEquals(200, response.code())
        assertFalse(response.body()?.result.isNullOrEmpty())
        assertEquals(1, response.body()?.current_page)
        assertTrue(response.body()?.total_page ?: 0 > 0)
    }

    @Test
    fun verifiesLanguagePaginationAndContentShape() = runBlocking {
        val response = api.getContentByLanguage(
            com.engineerfred.beststreamsug.data.remote.dto.ContentBrowseRequest(
                languageId = 36,
                pageNumber = 1,
            ),
        )

        assertEquals(200, response.code())
        assertFalse(response.body()?.result.isNullOrEmpty())
        assertEquals(1, response.body()?.current_page)
    }
}
