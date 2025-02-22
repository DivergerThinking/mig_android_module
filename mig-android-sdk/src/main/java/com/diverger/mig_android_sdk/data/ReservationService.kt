package com.diverger.mig_android_sdk.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import java.util.Date

interface ReservationService {
    @GET("gaming_space_reserves")
    suspend fun getReservationsByUser(
        @Query("filter[user][_eq]") userId: String,
        @Query("filter[status][_neq]") status: String = "cancelled",
        ///@Query("filter[date][_gte]") date: String = "\$NOW",
        @Query("filter[team][_null]") team: String = "true",
        @Query("fields") fields: String = "id,date,slot.*,qrImage,times.gaming_space_times_id.time,times.gaming_space_times_id.id",
        @Header("Authorization") token: String
    ): ReservationResponse

    @POST("gaming_space_reserves")
    suspend fun createReservation(
        @Body reservation: Reservation,
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
        @Query("fields") fields: String = "id,date,slot.*,times.gaming_space_times_id.time,times.gaming_space_times_id.id",
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
    private const val BASE_URL = "https://premig.randomkesports.com/cms/items/"
    private const val TOKEN = "Bearer 8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"

    val service: ReservationService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservationService::class.java)
    }

    suspend fun getReservations(userId: String): List<Reservation> {
        return service.getReservationsByUser(userId, token = TOKEN).data
    }

    // ✅ Obtener reservas de equipo
    suspend fun getReservationsByTeam(teamId: String): Result<List<Reservation>> {
        return try {
            val response = service.getReservationsByTeam(teamId, token = TOKEN)
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

    suspend fun createReservation(reservation: Reservation): Result<Unit> {
        return try {
            service.createReservation(reservation, token = TOKEN)
            Result.success(Unit) // ✅ Si se ejecuta correctamente, devolvemos `Result.success`
        } catch (e: Exception) {
            Result.failure(e) // ✅ Si hay error, devolvemos `Result.failure`
        }
    }

    suspend fun deleteReservation(id: Int) {
        service.deleteReservation(id, token = TOKEN)
    }
}

data class ReservationResponse(val data: List<Reservation>)

data class Reservation(
    val id: Int?,
    val status: String?,
    val slot: Slot,
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

/*data class Slot(
    val id: Int,
    val space: String
)*/

data class GamingSpaceTime(
    val id: Int,
    val time: String,
    val value: Int
)