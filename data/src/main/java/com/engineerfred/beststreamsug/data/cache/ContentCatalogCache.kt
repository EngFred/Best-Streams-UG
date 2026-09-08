package com.engineerfred.beststreamsug.data.cache

import com.engineerfred.beststreamsug.domain.model.ContentSummary
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentCatalogCache @Inject constructor() {

    private val cache = ConcurrentHashMap<Int, ContentSummary>()

    fun insertAll(items: List<ContentSummary>) {
        items.forEach { item ->
            cache[item.id] = item
        }
    }

    fun insert(item: ContentSummary) {
        cache[item.id] = item
    }

    fun getAll(): List<ContentSummary> = cache.values.toList()

    fun size(): Int = cache.size

    /**
     * Performs a client-side search across all indexed catalog items.
     * Searches title, VJ name, and description with tokenized matching and relevance ranking.
     */
    fun search(keyword: String): List<ContentSummary> {
        val rawQuery = keyword.trim().lowercase()
        if (rawQuery.isBlank()) return emptyList()

        val tokens = rawQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        return cache.values
            .asSequence()
            .filter { item ->
                val title = item.title.lowercase()
                val vj = item.vjName?.lowercase().orEmpty()
                val desc = item.description?.lowercase().orEmpty()

                // Every token must match either title, VJ, or description
                tokens.all { token ->
                    title.contains(token) ||
                    vj.contains(token) ||
                    desc.contains(token)
                }
            }
            .sortedWith(
                compareByDescending<ContentSummary> { it.title.equals(rawQuery, ignoreCase = true) }
                    .thenByDescending { it.title.startsWith(rawQuery, ignoreCase = true) }
                    .thenByDescending { it.title.contains(rawQuery, ignoreCase = true) }
                    .thenByDescending { it.releaseDate.orEmpty() }
            )
            .toList()
    }

    fun findByUrl(streamUrl: String): ContentSummary? {
        if (streamUrl.isBlank()) return null
        return cache.values.firstOrNull { it.defaultVideoUrl == streamUrl }
    }

    fun findByTitle(title: String): ContentSummary? {
        if (title.isBlank()) return null
        return cache.values.firstOrNull { it.title.equals(title, ignoreCase = true) }
    }
}
