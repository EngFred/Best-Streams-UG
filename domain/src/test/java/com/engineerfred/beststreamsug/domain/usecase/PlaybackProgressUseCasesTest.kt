package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.domain.model.PlaybackProgress
import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackProgressUseCasesTest {

    private class FakePlaybackProgressRepository : PlaybackProgressRepository {
        private val storage = mutableMapOf<String, PlaybackProgress>()

        override suspend fun saveProgress(mediaKey: String, positionMs: Long, durationMs: Long) {
            storage[mediaKey] = PlaybackProgress(
                mediaKey = mediaKey,
                positionMs = positionMs,
                durationMs = durationMs
            )
        }

        override suspend fun getProgress(mediaKey: String): PlaybackProgress? {
            return storage[mediaKey]
        }

        override suspend fun clearProgress(mediaKey: String) {
            storage.remove(mediaKey)
        }

        override suspend fun pruneExpiredProgress(maxAgeMillis: Long) {
            val cutoff = System.currentTimeMillis() - maxAgeMillis
            storage.entries.removeIf { it.value.updatedAtMillis < cutoff }
        }
    }

    @Test
    fun saveAndGetPlaybackProgressUseCase_savesAndRetrievesCorrectly() = runBlocking {
        val repository = FakePlaybackProgressRepository()
        val saveUseCase = SavePlaybackProgressUseCase(repository)
        val getUseCase = GetPlaybackProgressUseCase(repository)

        saveUseCase("https://stream.url/movie.mp4", 45_000L, 120_000L)
        val progress = getUseCase("https://stream.url/movie.mp4")

        assertEquals(45_000L, progress?.positionMs)
        assertEquals(120_000L, progress?.durationMs)
    }

    @Test
    fun clearPlaybackProgressUseCase_removesProgress() = runBlocking {
        val repository = FakePlaybackProgressRepository()
        val saveUseCase = SavePlaybackProgressUseCase(repository)
        val getUseCase = GetPlaybackProgressUseCase(repository)
        val clearUseCase = ClearPlaybackProgressUseCase(repository)

        saveUseCase("https://stream.url/movie.mp4", 45_000L, 120_000L)
        clearUseCase("https://stream.url/movie.mp4")

        val progress = getUseCase("https://stream.url/movie.mp4")
        assertNull(progress)
    }

    @Test
    fun blankMediaKey_isIgnored() = runBlocking {
        val repository = FakePlaybackProgressRepository()
        val saveUseCase = SavePlaybackProgressUseCase(repository)
        val getUseCase = GetPlaybackProgressUseCase(repository)
        val clearUseCase = ClearPlaybackProgressUseCase(repository)

        saveUseCase("", 45_000L, 120_000L)
        assertNull(getUseCase(""))
        clearUseCase("")
    }
}
