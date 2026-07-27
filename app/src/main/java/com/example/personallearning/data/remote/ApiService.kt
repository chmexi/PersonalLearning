package com.example.personallearning.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/daohen")
    suspend fun getEntry(@Query("date") date: String): Response<DaoHenDto?>

    @GET("api/daohen/range")
    suspend fun getRange(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null
    ): Response<List<DaoHenDto>>

    @GET("api/daohen/yesterday")
    suspend fun getYesterdayStone(): Response<YesterdayStoneDto?>

    @POST("api/daohen/sync")
    suspend fun syncEntry(@Body entry: DaoHenDto): Response<DaoHenDto>

    @POST("api/ai/daohen/analyze")
    suspend fun analyzeDaoHen(
        @Header("Authorization") authorization: String,
        @Body request: AnalyzeDaoHenRequest
    ): Response<AnalyzeDaoHenResponse>
}

data class AnalyzeDaoHenRequest(val transcript: String)
data class AnalyzeDaoHenResponse(
    val facts: List<String> = emptyList(),
    val emotions: List<EmotionDto> = emptyList(),
    val stone: StoneDto = StoneDto(),
    val betterChoice: BetterChoiceDto = BetterChoiceDto(),
    val questionForUser: String = ""
)
data class EmotionDto(val name: String = "", val intensity: Int = 0, val evidence: String = "")
data class StoneDto(val pattern: String = "", val confidence: Double = 0.0, val alternative: String = "")
data class BetterChoiceDto(val trigger: String = "", val action: String = "", val smallestStep: String = "")

data class DaoHenDto(
    val date: String,
    val q1: String? = "",
    val q2: String? = "",
    val q3: String? = "",
    val q4: String? = "",
    val q5: String? = "",
    val q6: String? = "",
    val q7: String? = "",
    val transcript: String? = "",
    val facts: String? = "",
    val emotions: String? = "",
    val stone: String? = "",
    val betterChoice: String? = "",
    val aiQuestion: String? = "",
    val analysisSource: String? = "",
    val analyzedAt: String? = "",
    val tags: String? = "",
    val actionStatus: Int = 0,
    val actionNote: String? = "",
    val id: Long = 0,
    val revision: Int = 0,
    val updatedAt: String? = null
)

data class YesterdayStoneDto(
    val date: String,
    val q6: String,
    val q1: String = ""
)
