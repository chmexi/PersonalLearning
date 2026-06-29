package com.example.personallearning.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/daohen")
    suspend fun getEntry(@Query("date") date: String): Response<DaoHenDto?>

    @GET("api/daohen/yesterday")
    suspend fun getYesterdayStone(): Response<YesterdayStoneDto?>

    @POST("api/daohen/sync")
    suspend fun syncEntry(@Body entry: DaoHenDto): Response<Map<String, Any>>
}

data class DaoHenDto(
    val date: String,
    val q1: String = "",
    val q2: String = "",
    val q3: String = "",
    val q4: String = "",
    val q5: String = "",
    val q6: String = "",
    val q7: String = "",
    val id: Long = 0
)

data class YesterdayStoneDto(
    val date: String,
    val q6: String,
    val q1: String = ""
)
