package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.repository.PlaybackRepository
import javax.inject.Inject

class GetPlaybackSourcesUseCase @Inject constructor(
    private val repository: PlaybackRepository,
) {
    suspend operator fun invoke(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<PlaybackMetadata> = repository.getContentPlayback(contentId, typeId, videoType)
}
