package com.engineerfred.beststreamsug.integration

import com.engineerfred.beststreamsug.BuildConfig
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.EpisodeRequest
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

class LiveEpisodesApiVerificationTest {
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
    fun verifiesSeasonEpisodesAndPlayback() = runBlocking {
        val response = api.getEpisodes(EpisodeRequest(showId = 381, seasonId = 1))
        val episodes = response.body()?.result.orEmpty()

        assertEquals(200, response.code())
        assertFalse(episodes.isEmpty())
        assertTrue(episodes.first().video320.orEmpty().isNotBlank())
        assertEquals(1, episodes.first().seasonId)
    }
}
