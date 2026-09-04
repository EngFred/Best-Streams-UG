package com.engineerfred.beststreamsug.domain.model

data class ContentDetails(
    val summary: ContentSummary,
    val categoryNames: List<String>,
    val languageName: String?,
    val cast: List<CastMember>,
    val playback: PlaybackMetadata,
    val seasons: List<Season>,
    val isUserLike: Boolean,
    val commentCount: Int,
)
