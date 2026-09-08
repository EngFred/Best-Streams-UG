package com.engineerfred.beststreamsug.domain.model

data class PlaybackProgress(
    val mediaKey: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
