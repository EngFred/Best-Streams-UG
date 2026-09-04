package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.repository.PlaybackRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPlaybackSourcesUseCaseTest {
    @Test
    fun delegatesToPlaybackRepository() = runBlocking {
        val repository = object : PlaybackRepository {
            override suspend fun getContentPlayback(contentId: Int, typeId: Int, videoType: Int) =
                AppResult.Success(PlaybackMetadata(emptyList(), null, emptyList()))
        }

        val result = GetPlaybackSourcesUseCase(repository)(1608, 1, 1)

        assertTrue(result is AppResult.Success)
    }
}
