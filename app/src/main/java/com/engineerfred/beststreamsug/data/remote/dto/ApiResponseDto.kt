package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponseDto<T>(
    val status: Int? = null,
    val message: String? = null,
    val result: T? = null,
    @SerializedName("total_rows")
    val total_rows: Int? = null,
    @SerializedName("total_page")
    val total_page: Int? = null,
    @SerializedName("current_page")
    val current_page: Int? = null,
    @SerializedName("more_page")
    val more_page: Boolean? = null,
)
