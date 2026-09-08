package com.engineerfred.beststreamsug.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {
    @Test
    fun successContainsData() {
        val result: AppResult<String> = AppResult.Success("value")

        assertEquals(AppResult.Success("value"), result)
    }

    @Test
    fun failureContainsApplicationError() {
        val result: AppResult<String> = AppResult.Failure(AppError.EmptyResponse)

        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.EmptyResponse, (result as AppResult.Failure).error)
    }
}
