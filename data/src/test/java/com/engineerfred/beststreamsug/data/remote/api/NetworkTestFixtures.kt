package com.engineerfred.beststreamsug.data.remote.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun createTestApiService(baseUrl: String): AppApiService {
    val client = OkHttpClient.Builder().build()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
        .create(AppApiService::class.java)
}
