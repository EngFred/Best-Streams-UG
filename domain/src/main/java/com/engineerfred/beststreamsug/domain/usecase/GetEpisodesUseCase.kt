package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.repository.EpisodeRepository
import javax.inject.Inject

class GetEpisodesUseCase @Inject constructor(
    private val repository: EpisodeRepository,
) {
    suspend operator fun invoke(showId: Int, seasonId: Int): AppResult<List<Episode>> =
        repository.getEpisodes(showId, seasonId)
}
