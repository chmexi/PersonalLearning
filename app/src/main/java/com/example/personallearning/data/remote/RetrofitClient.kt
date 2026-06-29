package com.example.personallearning.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var apiService: ApiService? = null
    private var currentBaseUrl: String = ""

    fun getApiService(baseUrl: String): ApiService {
        if (apiService == null || currentBaseUrl != normalizeUrl(baseUrl)) {
            currentBaseUrl = normalizeUrl(baseUrl)
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            apiService = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return apiService!!
    }

    private fun normalizeUrl(url: String): String {
        var n = url.trim()
        if (!n.startsWith("http://") && !n.startsWith("https://")) n = "http://$n"
        if (!n.endsWith("/")) n = "$n/"
        return n
    }
}
