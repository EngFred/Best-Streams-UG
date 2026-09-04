package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.EpisodeDto
import com.engineerfred.beststreamsug.domain.model.Episode
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.model.PlaybackSource
import com.engineerfred.beststreamsug.domain.model.Subtitle
import com.engineerfred.beststreamsug.domain.model.VideoQuality

fun EpisodeDto.toDomain(): Episode? {
    val episodeId = id ?: return null
    val episodeShowId = showId ?: return null
    val episodeSeasonId = seasonId ?: return null
    val episodeTitle = name?.takeIf { it.isNotBlank() } ?: return null

    return Episode(
        id = episodeId,
        showId = episodeShowId,
        seasonId = episodeSeasonId,
        title = episodeTitle,
        thumbnailUrl = thumbnail?.takeIf { it.isNotBlank() },
        landscapeUrl = landscape?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        durationMillis = videoDuration?.takeIf { it >= 0 },
        playback = PlaybackMetadata(
            sources = listOfNotNull(
                video320.toSource(VideoQuality.P320),
                video480.toSource(VideoQuality.P480),
                video720.toSource(VideoQuality.P720),
                video1080.toSource(VideoQuality.P1080),
            ),
            trailer = null,
            subtitles = listOfNotNull(
                subtitle(subtitleLanguage1, subtitle1, subtitleType),
                subtitle(subtitleLanguage2, subtitle2, subtitleType),
                subtitle(subtitleLanguage3, subtitle3, subtitleType),
            ),
        ),
        isPremium = isPremium == 1,
        viewCount = totalView ?: 0,
        sortOrder = sortOrder ?: 0,
    )
}

private fun String?.toSource(quality: VideoQuality): PlaybackSource? =
    this?.takeIf { it.isNotBlank() }?.let { PlaybackSource(quality, it) }

private fun subtitle(language: String?, url: String?, type: String?): Subtitle? {
    val subtitleLanguage = language?.takeIf { it.isNotBlank() } ?: return null
    val subtitleUrl = url?.takeIf { it.isNotBlank() } ?: return null
    return Subtitle(subtitleLanguage, type?.takeIf { it.isNotBlank() }, subtitleUrl)
}
