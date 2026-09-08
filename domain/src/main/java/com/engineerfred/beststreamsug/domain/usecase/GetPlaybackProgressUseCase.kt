package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.domain.model.PlaybackProgress
import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import javax.inject.Inject

class GetPlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackProgressRepository
) {
    suspend operator fun invoke(mediaKey: String): PlaybackProgress? {
        if (mediaKey.isBlank()) return null
        return repository.getProgress(mediaKey)
    }
}
