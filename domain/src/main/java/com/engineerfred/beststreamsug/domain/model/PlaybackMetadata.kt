package com.engineerfred.beststreamsug.domain.model

data class PlaybackMetadata(
    val sources: List<PlaybackSource>,
    val trailer: Trailer?,
    val subtitles: List<Subtitle>,
)
