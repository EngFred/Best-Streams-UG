package com.engineerfred.beststreamsug.mobile.core.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotifiedContentTracker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isInitialized(): Boolean = prefs.getBoolean(KEY_IS_INITIALIZED, false)

    fun setInitialized(initialized: Boolean) {
        prefs.edit().putBoolean(KEY_IS_INITIALIZED, initialized).apply()
    }

    fun hasBeenNotified(contentId: Int): Boolean {
        val notifiedIds = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet()) ?: emptySet()
        return notifiedIds.contains(contentId.toString())
    }

    fun markAsNotified(contentId: Int) {
        val currentIds = (prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet()) ?: emptySet()).toMutableSet()
        currentIds.add(contentId.toString())
        // Keep maximum 500 IDs to avoid unbounded growth
        val trimmed = if (currentIds.size > MAX_STORED_IDS) {
            currentIds.toList().takeLast(MAX_STORED_IDS).toSet()
        } else {
            currentIds
        }
        prefs.edit().putStringSet(KEY_NOTIFIED_IDS, trimmed).apply()
    }

    fun markBatchAsNotified(contentIds: Collection<Int>) {
        val currentIds = (prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet()) ?: emptySet()).toMutableSet()
        currentIds.addAll(contentIds.map { it.toString() })
        val trimmed = if (currentIds.size > MAX_STORED_IDS) {
            currentIds.toList().takeLast(MAX_STORED_IDS).toSet()
        } else {
            currentIds
        }
        prefs.edit().putStringSet(KEY_NOTIFIED_IDS, trimmed).apply()
    }

    private companion object {
        const val PREFS_NAME = "best_streams_notifications_prefs"
        const val KEY_IS_INITIALIZED = "is_content_tracker_initialized"
        const val KEY_NOTIFIED_IDS = "notified_content_ids"
        const val MAX_STORED_IDS = 500
    }
}
