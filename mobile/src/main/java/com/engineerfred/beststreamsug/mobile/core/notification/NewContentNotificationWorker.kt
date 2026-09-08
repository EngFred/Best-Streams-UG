package com.engineerfred.beststreamsug.mobile.core.notification

import android.content.Context
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
                allItems.addAll(movieSections.data.flatMap { it.items })
            }
            if (seriesSections is AppResult.Success) {
                allItems.addAll(seriesSections.data.flatMap { it.items })
            }

            val uniqueItems = allItems.distinctBy { it.id }

            val itemsToNotify: List<ContentSummary>
            if (!tracker.isInitialized()) {
                // On first launch, notify the single latest release so user can verify, then index the rest
                val firstItem = uniqueItems.firstOrNull()
                itemsToNotify = if (firstItem != null) listOf(firstItem) else emptyList()
                tracker.markBatchAsNotified(uniqueItems.map { it.id })
                tracker.setInitialized(true)
            } else {
                // On subsequent runs, identify genuinely new items
                val newItems = uniqueItems.filter { !tracker.hasBeenNotified(it.id) }
                itemsToNotify = newItems.take(MAX_NOTIFICATIONS_PER_RUN)
                if (newItems.size > itemsToNotify.size) {
                    tracker.markBatchAsNotified(newItems.map { it.id })
                }
            }

            for (item in itemsToNotify) {
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
            }

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    private companion object {
        const val MAX_NOTIFICATIONS_PER_RUN = 3
    }
}

private fun ContentKind.apiTypeId(): Int = when (this) {
    ContentKind.MOVIE -> 1
    ContentKind.TV_SHOW -> 2
    ContentKind.UNKNOWN -> 0
}
