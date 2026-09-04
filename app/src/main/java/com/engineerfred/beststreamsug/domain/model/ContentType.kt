package com.engineerfred.beststreamsug.domain.model

data class ContentType(
    val id: Int,
    val name: String,
    val type: Int,
    val iconUrl: String?,
    val sortOrder: Int,
)
