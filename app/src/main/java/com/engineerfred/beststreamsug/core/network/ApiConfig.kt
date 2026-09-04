package com.engineerfred.beststreamsug.core.network

import com.engineerfred.beststreamsug.BuildConfig

object ApiConfig {
    val BASE_URL: String = BuildConfig.API_BASE_URL
    const val CONNECT_TIMEOUT_SECONDS = 10L
    const val READ_TIMEOUT_SECONDS = 40L
    const val WRITE_TIMEOUT_SECONDS = 40L
    const val CALL_TIMEOUT_SECONDS = 75L
}
