package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.domain.model.RecentlyAddedRails
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class GetRecentlyAddedContentUseCase @Inject constructor(
    private val browseRepository: BrowseRepository,
) {
    suspend operator fun invoke(): RecentlyAddedRails = coroutineScope {
        val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)
        val results = (1..MAX_CATEGORY_ID).map { categoryId ->
            async {
                semaphore.withPermit {
                    browseRepository.getContentByCategory(
                        categoryId = categoryId,
                        pageNumber = 1,
                        sort = com.engineerfred.beststreamsug.domain.model.BrowseSort.Newest,
                    )
                }
            }
        }.awaitAll()
        val successes = results.mapNotNull {
            (it as? AppResult.Success)?.data?.items
        }.flatten()
        val firstError = results.firstNotNullOfOrNull {
            (it as? AppResult.Failure)?.error
        }
        val allContent = successes
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt.orEmpty() }

        RecentlyAddedRails(
            movies = recentResult(
                allContent.filter { it.kind == ContentKind.MOVIE }.take(MAX_ITEMS),
                firstError,
            ),
            series = recentResult(
                allContent.filter { it.kind == ContentKind.TV_SHOW }.take(MAX_ITEMS),
                firstError,
            ),
        )
    }

    private fun recentResult(
        items: List<ContentSummary>,
        error: AppError?,
    ): AppResult<List<ContentSummary>> =
        if (items.isNotEmpty() || error == null) AppResult.Success(items)
        else AppResult.Failure(error)

    private companion object {
        const val MAX_CATEGORY_ID = 30
        const val MAX_CONCURRENT_REQUESTS = 3
        const val MAX_ITEMS = 20
    }
}
