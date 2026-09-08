package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.domain.model.PlaybackProgress

interface PlaybackProgressRepository {
    suspend fun saveProgress(mediaKey: String, positionMs: Long, durationMs: Long)
    suspend fun getProgress(mediaKey: String): PlaybackProgress?
    suspend fun clearProgress(mediaKey: String)
    suspend fun pruneExpiredProgress(maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L)
}
