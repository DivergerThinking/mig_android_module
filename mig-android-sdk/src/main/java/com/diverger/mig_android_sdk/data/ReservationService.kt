package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Date

interface ReservationService {
    @GET("gaming_space_reserves")
    suspend fun getReservationsByUser(
        @Query("filter[user][_eq]") userId: String,
        @Query("filter[status][_neq]") status: String = "cancelled",
        @Query("filter[date][_gte]") date: String = "\$NOW",
        @Query("filter[team][_null]") team: String = "true",
        @Query("fields") fields: String = "id,date,slot.*,qrImage,qrValue,times.gaming_space_times_id.time,times.gaming_space_times_id.id,slot.space.*,slot.space.translations.*",
        @Header("Authorization") token: String
    ): ReservationResponse

    @POST("gaming_space_reserves")
    suspend fun createReservation(
        @Body reservation: ReservationWrapper,
        @Header("Authorization") token: String
    ): Unit

    @DELETE("gaming_space_reserves/{id}")
    suspend fun deleteReservation(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Unit

    // 📌 Obtener reservas de equipo
    @GET("gaming_space_reserves")
    suspend fun getReservationsByTeam(
        @Query("filter[team][_eq]") teamId: String,
        @Query("filter[status][_neq]") status: String = "cancelled",
        @Query("fields") fields: String = "id,date,slot.*,times.gaming_space_times_id.time,times.gaming_space_times_id.id,slot.space.*,slot.space.translations.*",
        @Header("Authorization") token: String
    ): ReservationResponse

    // 📌 Obtener reservas de equipo filtradas por usuario
    @GET("gaming_space_reserves")
    suspend fun getReservationsByTeamAndUser(
        @Query("filter[team][_eq]") teamId: String,
        @Query("filter[user][_eq]") userId: String,
        @Query("filter[status][_neq]") status: String = "cancelled",
        @Query("fields") fields: String = "id,date,slot.*,times.gaming_space_times_id.time,times.gaming_space_times_id.id",
        @Header("Authorization") token: String
    ): ReservationResponse
}

object ReservationApi {
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private val TOKEN = "Bearer ${UserManager.getAccessToken()}"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = getUnsafeOkHttpClient()
        .newBuilder()
        .addInterceptor(loggingInterceptor)
        .build()

    val service: ReservationService by lazy {
        Retrofit.Builder()
            .client(httpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservationService::class.java)
    }

    suspend fun getReservations(userId: String): Result<List<Reservation>> {
        return try {
            val response = service.getReservationsByUser(userId, token = TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Obtener reservas de equipo
    suspend fun getReservationsByTeam(teamId: String): Result<List<Reservation>> {
        return try {
            val response = service.getReservationsByTeam("6d8fd820-5aa4-479a-89e9-1f85c906f189", token = TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Obtener reservas de equipo filtradas por usuario
    suspend fun getReservationsByTeamAndUser(teamId: String, userId: String): Result<List<Reservation>> {
        return try {
            val response = service.getReservationsByTeamAndUser(teamId, userId, token = TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createReservation(reservation: ReservationWrapper): Result<Unit> {
        return try {
            service.createReservation(reservation, token = TOKEN)
            Result.success(Unit) // ✅ Si se ejecuta correctamente, devolvemos `Result.success`
        } catch (e: Exception) {
            Result.failure(e) // ✅ Si hay error, devolvemos `Result.failure`
        }
    }

    suspend fun deleteReservation(id: Int): Result<Unit> {
        return try {
            service.deleteReservation(id, token = TOKEN)
            Result.success(Unit) // ✅ Si se ejecuta correctamente, devolvemos `Result.success`
        } catch (e: Exception) {
            Result.failure(e) // ❌ Si hay error, devolvemos `Result.failure`
        }
    }
}

data class ReservationResponse(val data: List<Reservation>)

data class Reservation(
    val id: Int?,
    val status: String?,
    val slot: SlotReservation,
    val date: String,
    val user: String?,
    val team: String?,
    val training: String?,
    @SerializedName("qrImage") val qrImage: String?,
    @SerializedName("qrValue") val qrValue: String?,
    @SerializedName("times") val timesContainer: List<Map<String, GamingSpaceTime>>,
    @SerializedName("peripheral_loans") val peripheralLoans: List<Int>?,
) {
    // Extraemos `gaming_space_times_id` correctamente
    val times: List<GamingSpaceTime> get() = timesContainer.mapNotNull { it["gaming_space_times_id"] }
}

data class ReservationWrapper(
    val id: Int?,
    val status: String?,
    val slot: Int,
    val date: String,
    val user: String?,
    val team: String?,
    val training: String?,
    @SerializedName("qrImage") val qrImage: String?,
    @SerializedName("qrValue") val qrValue: String?,
    @SerializedName("times") val timesContainer: List<Map<String, GamingSpaceTimeId>>,
    @SerializedName("peripheral_loans") val peripheralLoans: List<Int>?,
) {
    // Extraemos `gaming_space_times_id` correctamente
    val times: List<GamingSpaceTimeId> get() = timesContainer.mapNotNull { it["gaming_space_times_id"] }
}

/*data class Slot(
    val id: Int,
    val space: String
)*/

data class GamingSpaceTime(
    val id: Int,
    val time: String,
    val value: Int
)

data class GamingSpaceTimeId(
    val id: Int
)

data class SlotId(
    val id:Int
)