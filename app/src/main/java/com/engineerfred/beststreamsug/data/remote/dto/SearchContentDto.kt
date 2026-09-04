package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchContentRequest(
    val keyword: String,
    @SerializedName("language_id")
    val languageId: Int? = null,
    @SerializedName("page_no")
    val pageNumber: Int = 1,
)
