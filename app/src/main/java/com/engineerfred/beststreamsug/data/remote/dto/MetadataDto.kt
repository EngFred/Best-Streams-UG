package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
)

data class ContentTypeDto(
    val id: Int? = null,
    val name: String? = null,
    val type: Int? = null,
    val icon: String? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
)

data class LanguageDto(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
)
