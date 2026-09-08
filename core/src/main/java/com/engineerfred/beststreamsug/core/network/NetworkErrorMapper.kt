package com.engineerfred.beststreamsug.core.network

import com.engineerfred.beststreamsug.core.common.AppError
import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorMapper {
    fun map(throwable: Throwable): AppError = when (throwable) {
        is SocketTimeoutException -> AppError.Timeout
        is UnknownHostException -> AppError.NetworkUnavailable
        is JsonParseException -> AppError.Serialization(throwable.message)
        is IOException -> AppError.NetworkUnavailable
        else -> AppError.Unknown(throwable)
    }

    fun mapHttpStatus(statusCode: Int, message: String? = null): AppError = AppError.Http(
        statusCode = statusCode,
        message = message,
    )
}
