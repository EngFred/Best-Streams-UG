package com.engineerfred.beststreamsug.core.common

sealed interface AppError {
    data object NetworkUnavailable : AppError

    data object Timeout : AppError

    data class Http(
        val statusCode: Int,
        val message: String? = null,
    ) : AppError

    data class Serialization(
        val message: String? = null,
    ) : AppError

    data object EmptyResponse : AppError

    data class InvalidResponse(
        val message: String? = null,
    ) : AppError

    data class Unknown(
        val cause: Throwable? = null,
    ) : AppError
}
