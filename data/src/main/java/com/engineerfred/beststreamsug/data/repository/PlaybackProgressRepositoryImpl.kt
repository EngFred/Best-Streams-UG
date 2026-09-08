package com.engineerfred.beststreamsug.data.repository

import android.content.Context
import com.engineerfred.beststreamsug.domain.model.PlaybackProgress
import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackProgressRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : PlaybackProgressRepository {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun saveProgress(
        mediaKey: String,
        positionMs: Long,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
        if (mediaKey.isBlank()) return@withContext

        val key = hashKey(mediaKey)

        // If played less than 5 seconds, do not save
        if (positionMs < MIN_SAVE_THRESHOLD_MS) {
            return@withContext
        }

        // If content is nearly finished (>= 95% or within last 30 seconds), clear saved progress
        val isNearEnd = durationMs > 0L && (
            positionMs >= (durationMs - NEAR_END_THRESHOLD_MS) ||
            (positionMs.toFloat() / durationMs.toFloat()) >= 0.95f
        )

        if (isNearEnd) {
            prefs.edit().remove(key).apply()
            return@withContext
        }

        val entity = PlaybackProgressEntity(
            mediaKey = mediaKey,
            positionMs = positionMs,
            durationMs = durationMs,
            updatedAtMillis = System.currentTimeMillis()
        )

        val json = gson.toJson(entity)
        prefs.edit().putString(key, json).apply()

        // Periodically prune stale entries older than 7 days
        pruneExpiredProgressInternal()
    }

    override suspend fun getProgress(
        mediaKey: String
    ): PlaybackProgress? = withContext(Dispatchers.IO) {
        if (mediaKey.isBlank()) return@withContext null

        val key = hashKey(mediaKey)
        val json = prefs.getString(key, null) ?: return@withContext null

        try {
            val entity = gson.fromJson(json, PlaybackProgressEntity::class.java) ?: return@withContext null
            val now = System.currentTimeMillis()

            // Check 7-day expiration
            if (now - entity.updatedAtMillis > MAX_AGE_MILLIS) {
                prefs.edit().remove(key).apply()
                return@withContext null
            }

            // Check if position was less than 5 seconds or near end
            val isNearEnd = entity.durationMs > 0L && (
                entity.positionMs >= (entity.durationMs - NEAR_END_THRESHOLD_MS) ||
                (entity.positionMs.toFloat() / entity.durationMs.toFloat()) >= 0.95f
            )

            if (entity.positionMs < MIN_SAVE_THRESHOLD_MS || isNearEnd) {
                prefs.edit().remove(key).apply()
                return@withContext null
            }

            PlaybackProgress(
                mediaKey = entity.mediaKey,
                positionMs = entity.positionMs,
                durationMs = entity.durationMs,
                updatedAtMillis = entity.updatedAtMillis
            )
        } catch (e: Exception) {
            prefs.edit().remove(key).apply()
            null
        }
    }

    override suspend fun clearProgress(
        mediaKey: String
    ) = withContext(Dispatchers.IO) {
        if (mediaKey.isBlank()) return@withContext
        val key = hashKey(mediaKey)
        prefs.edit().remove(key).apply()
    }

    override suspend fun pruneExpiredProgress(
        maxAgeMillis: Long
    ) = withContext(Dispatchers.IO) {
        pruneExpiredProgressInternal(maxAgeMillis)
    }

    private fun pruneExpiredProgressInternal(maxAgeMillis: Long = MAX_AGE_MILLIS) {
        try {
            val now = System.currentTimeMillis()
            val allEntries = prefs.all
            val editor = prefs.edit()
            var modified = false

            for ((key, value) in allEntries) {
                if (value is String) {
                    try {
                        val entity = gson.fromJson(value, PlaybackProgressEntity::class.java)
                        if (entity == null || (now - entity.updatedAtMillis > maxAgeMillis)) {
                            editor.remove(key)
                            modified = true
                        }
                    } catch (e: Exception) {
                        editor.remove(key)
                        modified = true
                    }
                }
            }
            if (modified) {
                editor.apply()
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun hashKey(rawKey: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(rawKey.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            rawKey.hashCode().toString()
        }
    }

    private data class PlaybackProgressEntity(
        @SerializedName("media_key") val mediaKey: String,
        @SerializedName("position_ms") val positionMs: Long,
        @SerializedName("duration_ms") val durationMs: Long,
        @SerializedName("updated_at_millis") val updatedAtMillis: Long
    )

    companion object {
        private const val PREFS_NAME = "beststreams_playback_progress"
        private const val MIN_SAVE_THRESHOLD_MS = 5_000L // 5 seconds
        private const val NEAR_END_THRESHOLD_MS = 30_000L // 30 seconds from end
        private const val MAX_AGE_MILLIS = 7 * 24 * 60 * 60 * 1000L // 7 days (1 week)
    }
}
