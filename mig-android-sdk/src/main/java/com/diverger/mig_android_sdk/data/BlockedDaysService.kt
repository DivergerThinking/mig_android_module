package com.diverger.mig_android_sdk.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.annotations.SerializedName

interface BlockedDaysApi {
    @GET("gaming_space_blocked_days")
    suspend fun getBlockedDates(
        @Query("fields") fields: String = "*",
        @Query("filter[date][_gte]") minDate: String,
        @Header("Authorization") token: String
    ): BlockedDatesResponse
}

object BlockedDaysService {
    private const val BASE_URL = "https://webesports.madridingame.es/cms/items/"
    private const val TOKEN = "Bearer 8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"

    private val service: BlockedDaysApi by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BlockedDaysApi::class.java)
    }

    suspend fun fetchBlockedDates(): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val minDate = getCurrentDate() // Obtener la fecha actual en formato "yyyy-MM-dd"
                val response = service.getBlockedDates(minDate = minDate, token = TOKEN)
                val blockedDates = response.data.map { it.date }
                Result.success(blockedDates)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getCurrentDate(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
}

data class BlockedDatesResponse(
    val data: List<BlockedDate>
)

data class BlockedDate(
    val id: Int,
    val date: String,
    val description: String?
)