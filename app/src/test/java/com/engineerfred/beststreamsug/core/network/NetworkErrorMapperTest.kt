package com.engineerfred.beststreamsug.core.network

import com.engineerfred.beststreamsug.core.common.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorMapperTest {
    private val mapper = NetworkErrorMapper()

    @Test
    fun mapsTimeoutToTimeoutError() {
        assertEquals(AppError.Timeout, mapper.map(SocketTimeoutException()))
    }

    @Test
    fun mapsUnknownHostToNetworkUnavailable() {
        assertEquals(AppError.NetworkUnavailable, mapper.map(UnknownHostException()))
    }

    @Test
    fun mapsIoExceptionToNetworkUnavailable() {
        assertEquals(AppError.NetworkUnavailable, mapper.map(IOException()))
    }

    @Test
    fun mapsHttpStatusToHttpError() {
        assertEquals(
            AppError.Http(statusCode = 500, message = "server error"),
            mapper.mapHttpStatus(500, "server error"),
        )
    }
}
