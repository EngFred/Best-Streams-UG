package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

class SavePlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    suspend operator fun invoke(mediaKey: String, positionMs: Long, durationMs: Long) {
        if (mediaKey.isBlank()) return
        repository.saveProgress(mediaKey, positionMs, durationMs)
    }
}
