package com.engineerfred.beststreamsug.domain.model

data class Episode(
    val id: Int,
    val showId: Int,
    val seasonId: Int,
    val title: String,
    val thumbnailUrl: String?,
    val landscapeUrl: String?,
    val description: String?,
    val durationMillis: Long?,
    val playback: PlaybackMetadata,
    val isPremium: Boolean,
    val viewCount: Int,
    val sortOrder: Int,
)
