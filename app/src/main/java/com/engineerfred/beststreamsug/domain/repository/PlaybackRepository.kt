package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata

interface PlaybackRepository {
    suspend fun getContentPlayback(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<PlaybackMetadata>
}
