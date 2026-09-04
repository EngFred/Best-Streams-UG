package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SectionRequest(
    @SerializedName("is_home_screen")
    val isHomeScreen: Int,
    @SerializedName("type_id")
    val typeId: Int,
)

data class SectionDto(
    val id: Int? = null,
    val name: String? = null,
    val title: String? = null,
    @SerializedName("section_name")
    val sectionName: String? = null,
    @SerializedName("section_title")
    val sectionTitle: String? = null,
    @SerializedName("screen_layout")
    val screenLayout: String? = null,
    val data: List<ContentDto>? = null,
)
