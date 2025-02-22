package com.diverger.mig_android_sdk.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.Exception

// --------------------------- INTERFAZ API ---------------------------
interface WeekTimeApiService {
    @GET("gaming_space_week_times")
    suspend fun getWeekTimes(
        @Query("filter[value][_eq]") dayValue: Int,
        @Query("fields") fields: String = "*,times.gaming_space_times_id.time,times.gaming_space_times_id.id,times.gaming_space_times_id.value",
        @Header("Authorization") token: String
    ): WeekTimeResponse
}

// --------------------------- SERVICIO DE SEMANA ---------------------------
object WeekTimeService {
    private const val BASE_URL = "https://premig.randomkesports.com/cms/items/"
    private const val TOKEN = "Bearer 8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"

    private val api: WeekTimeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeekTimeApiService::class.java)
    }

    suspend fun fetchWeekTimeByDay(dayValue: Int): Result<List<GamingSpaceTime>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getWeekTimes(dayValue, token = TOKEN)
            }
            val times = response.data.flatMap { weekData ->
                weekData.times.map { it.gamingSpaceTime }
            }.sortedBy { it.value } // Ordena por `value` como en iOS

            Result.success(times)
        } catch (e: Exception) {
            Log.e("WeekTimeService", "Error obteniendo horarios: ${e.message}")
            Result.failure(e)
        }
    }
}

// --------------------------- MODELOS DE RESPUESTA ---------------------------
data class WeekTimeResponse(
    val data: List<WeekData>
)

data class WeekData(
    val id: Int,
    val weekday: String,
    val value: Int,
    val times: List<GamingTime>
)

data class GamingTime(
    @SerializedName("gaming_space_times_id") val gamingSpaceTime: GamingSpaceTime
)
