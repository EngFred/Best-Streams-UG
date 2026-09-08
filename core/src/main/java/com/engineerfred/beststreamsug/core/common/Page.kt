package com.engineerfred.beststreamsug.core.common

data class Page<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val hasMore: Boolean,
)
