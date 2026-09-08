package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Episode

interface EpisodeRepository {
    suspend fun getEpisodes(
        showId: Int,
        seasonId: Int,
    ): AppResult<List<Episode>>
}
