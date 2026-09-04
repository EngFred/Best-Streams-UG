package com.engineerfred.beststreamsug.domain.model

data class ContentSection(
    val id: Int,
    val title: String,
    val items: List<ContentSummary>,
    val layout: String? = null,
)
