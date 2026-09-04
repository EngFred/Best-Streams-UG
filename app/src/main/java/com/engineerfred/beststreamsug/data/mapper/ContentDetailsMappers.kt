package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.CastDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailsDto
import com.engineerfred.beststreamsug.data.remote.dto.SeasonDto
import com.engineerfred.beststreamsug.domain.model.CastMember
import com.engineerfred.beststreamsug.domain.model.ContentDetails
import com.engineerfred.beststreamsug.domain.model.PlaybackMetadata
import com.engineerfred.beststreamsug.domain.model.PlaybackSource
import com.engineerfred.beststreamsug.domain.model.Season
import com.engineerfred.beststreamsug.domain.model.Subtitle
import com.engineerfred.beststreamsug.domain.model.Trailer
import com.engineerfred.beststreamsug.domain.model.VideoQuality

fun ContentDetailsDto.toDomain(): ContentDetails? {
    val summary = ContentDto(
        id = id,
        typeId = typeId,
        videoType = videoType,
        name = name,
        thumbnail = thumbnail,
        landscape = landscape,
        description = description,
        videoDuration = videoDuration,
        video320 = video320,
        video480 = video480,
        video720 = video720,
        video1080 = video1080,
        releaseDate = releaseDate,
        isPremium = isPremium,
        totalView = totalView,
        totalLike = totalLike,
        averageRating = averageRating,
        totalReview = totalReview,
        languageName = languageName,
    ).toDomain() ?: return null

    return ContentDetails(
        summary = summary,
        categoryNames = categoryName.toNames(),
        languageName = languageName?.takeIf { it.isNotBlank() },
        cast = cast.orEmpty().mapNotNull { it.toDomain() },
        playback = PlaybackMetadata(
            sources = listOfNotNull(
                video320.toSource(VideoQuality.P320),
                video480.toSource(VideoQuality.P480),
                video720.toSource(VideoQuality.P720),
                video1080.toSource(VideoQuality.P1080),
            ),
            trailer = Trailer(
                type = trailerType?.takeIf { it.isNotBlank() },
                url = trailerUrl?.takeIf { it.isNotBlank() },
            ).takeIf { it.type != null || it.url != null },
            subtitles = listOfNotNull(
                subtitle(languageName = subtitleLanguage1, url = subtitle1, type = subtitleType),
                subtitle(languageName = subtitleLanguage2, url = subtitle2, type = subtitleType),
                subtitle(languageName = subtitleLanguage3, url = subtitle3, type = subtitleType),
            ),
        ),
        seasons = season.orEmpty().mapNotNull { it.toDomain() },
        isUserLike = isUserLike == 1,
        commentCount = totalComment ?: 0,
    )
}

private fun CastDto.toDomain(): CastMember? {
    val castId = id ?: return null
    val castName = name?.takeIf { it.isNotBlank() } ?: return null
    return CastMember(castId, castName, image?.takeIf { it.isNotBlank() }, type, personalInfo)
}

private fun SeasonDto.toDomain(): Season? {
    val seasonId = id ?: return null
    val seasonName = name?.takeIf { it.isNotBlank() } ?: return null
    return Season(seasonId, seasonName, sortOrder ?: 0)
}

private fun String?.toNames(): List<String> = this
    ?.split(",")
    ?.map(String::trim)
    ?.filter(String::isNotBlank)
    .orEmpty()

private fun String?.toSource(quality: VideoQuality): PlaybackSource? =
    this?.takeIf { it.isNotBlank() }?.let { PlaybackSource(quality, it) }

private fun subtitle(
    languageName: String?,
    url: String?,
    type: String?,
): Subtitle? {
    val language = languageName?.takeIf { it.isNotBlank() } ?: return null
    val subtitleUrl = url?.takeIf { it.isNotBlank() } ?: return null
    return Subtitle(language, type?.takeIf { it.isNotBlank() }, subtitleUrl)
}
