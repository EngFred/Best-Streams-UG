package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

class ClearPlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    suspend operator fun invoke(mediaKey: String) {
        if (mediaKey.isBlank()) return
        repository.clearProgress(mediaKey)
    }
}
