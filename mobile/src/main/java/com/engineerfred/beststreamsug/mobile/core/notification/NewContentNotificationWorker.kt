package com.engineerfred.beststreamsug.mobile.core.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import com.engineerfred.beststreamsug.domain.usecase.GetContentDetailsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@HiltWorker
class NewContentNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sectionRepository: SectionRepository,
    private val getContentDetailsUseCase: GetContentDetailsUseCase,
    private val tracker: NotifiedContentTracker,
    private val notificationManager: ContentNotificationManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "=== NewContentNotificationWorker started ===")
        try {
            // Fetch movie and series sections concurrently
            val movieSectionsDeferred = async {
                sectionRepository.getSections(isHomeScreen = 1, typeId = 1)
            }
            val seriesSectionsDeferred = async {
                sectionRepository.getSections(isHomeScreen = 1, typeId = 2)
            }

            val movieSections = movieSectionsDeferred.await()
            val seriesSections = seriesSectionsDeferred.await()

            val allItems = mutableListOf<ContentSummary>()
            if (movieSections is AppResult.Success) {
                val movieItems = movieSections.data.flatMap { it.items }
                allItems.addAll(movieItems)
                Log.d(TAG, "Fetched ${movieSections.data.size} movie sections with ${movieItems.size} items")
            } else if (movieSections is AppResult.Failure) {
                Log.w(TAG, "Failed to fetch movie sections: ${movieSections.error}")
            }

            if (seriesSections is AppResult.Success) {
                val seriesItems = seriesSections.data.flatMap { it.items }
                allItems.addAll(seriesItems)
                Log.d(TAG, "Fetched ${seriesSections.data.size} series sections with ${seriesItems.size} items")
            } else if (seriesSections is AppResult.Failure) {
                Log.w(TAG, "Failed to fetch series sections: ${seriesSections.error}")
            }

            if (movieSections is AppResult.Failure && seriesSections is AppResult.Failure) {
                Log.w(TAG, "Both movie and series section fetches failed. Retrying later...")
                return@coroutineScope Result.retry()
            }

            val uniqueItems = allItems.distinctBy { it.id }
            Log.d(TAG, "Total unique items in catalog: ${uniqueItems.size}")

            val itemsToNotify: List<ContentSummary>
            if (!tracker.isInitialized()) {
                Log.i(TAG, "First-time worker initialization. Indexing catalog...")
                val firstItem = uniqueItems.firstOrNull()
                itemsToNotify = if (firstItem != null) listOf(firstItem) else emptyList()
                tracker.markBatchAsNotified(uniqueItems.map { it.id })
                tracker.setInitialized(true)
                Log.i(TAG, "Indexed ${uniqueItems.size} items. Selected sample notification: ${firstItem?.title}")
            } else {
                val newItems = uniqueItems.filter { !tracker.hasBeenNotified(it.id) }
                itemsToNotify = newItems.take(MAX_NOTIFICATIONS_PER_RUN)
                Log.i(TAG, "Detected ${newItems.size} genuinely new items. Preparing to notify ${itemsToNotify.size} items.")
                if (newItems.size > itemsToNotify.size) {
                    tracker.markBatchAsNotified(newItems.map { it.id })
                }
            }

            for (item in itemsToNotify) {
                Log.d(TAG, "Fetching full details for notification: '${item.title}' (ID: ${item.id})")
                val detailsResult = getContentDetailsUseCase(
                    contentId = item.id,
                    typeId = item.kind.apiTypeId(),
                    videoType = item.kind.apiTypeId(),
                )

                val (genres, overview, posterUrl) = if (detailsResult is AppResult.Success) {
                    val details = detailsResult.data
                    Triple(
                        details.categoryNames,
                        details.summary.description ?: item.description,
                        details.summary.landscapeUrl ?: details.summary.thumbnailUrl ?: item.landscapeUrl ?: item.thumbnailUrl,
                    )
                } else {
                    Triple(
                        emptyList(),
                        item.description,
                        item.landscapeUrl ?: item.thumbnailUrl,
                    )
                }

                notificationManager.showNewContentNotification(
                    contentId = item.id,
                    kind = item.kind,
                    title = item.title,
                    vjName = item.vjName,
                    genres = genres,
                    overview = overview,
                    posterUrl = posterUrl,
                )

                tracker.markAsNotified(item.id)
                Log.i(TAG, "Successfully posted notification for '${item.title}'")
            }

            Log.i(TAG, "=== NewContentNotificationWorker finished successfully ===")
            Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, "Error in NewContentNotificationWorker", e)
            Result.retry()
        }
    }

    private companion object {
        const val TAG = "NewContentWorker"
        const val MAX_NOTIFICATIONS_PER_RUN = 3
    }
}

private fun ContentKind.apiTypeId(): Int = when (this) {
    ContentKind.MOVIE -> 1
    ContentKind.TV_SHOW -> 2
    ContentKind.UNKNOWN -> 0
}
