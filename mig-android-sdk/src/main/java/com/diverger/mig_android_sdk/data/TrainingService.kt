package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface TrainingService {
    @GET("trainings")
    suspend fun getAllTrainings(
        @Query("filter[team][_eq]") teamId: String,
        @Query("fields") fields: String = "id,start_date,time,players.users_id.id,players.users_id.avatar,players.users_id.email,players.users_id.first_name,type,reserves.*,reserves.team.name,reserves.team.picture,reserves.times.gaming_space_times_id.time,notes",
        @Header("Authorization") token: String
    ): TrainingResponse
}

object TrainingApi {
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private val TOKEN = "Bearer ${UserManager.getAccessToken()}"

    val service: TrainingService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrainingService::class.java)
    }

    suspend fun getTrainings(teamId: String): Result<List<EventModel>> {
        return try {
            val response = service.getAllTrainings(teamId, token = TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class TrainingResponse(val data: List<EventModel>)

data class EventModel(
    val id: String?,
    val notes: String?,
    val players: List<PlayerUsersModel>?,
    val reserves: List<Reserve>?,
    @SerializedName("start_date") val startDate: String,
    val status: String,
    val time: String,
    val type: String
)

data class Reserve(
    val date: String?,
    val id: Int?,
    @SerializedName("qrImage") val qrImage: String?,
    @SerializedName("qrValue") val qrValue: String?,
    val slot: Int?,
    val status: String?,
    val team: Team?,
    val times: List<Time>?,
    val training: String?,
    val user: String?
)

data class Time(
    @SerializedName("gaming_space_times_id") val gamingSpaceTimesID: GamingSpaceTimesID?
)

data class GamingSpaceTimesID(
    val time: String?
)

data class PlayerUsersModel(
    @SerializedName("users_id") val userId: PlayerModel?
)

data class PlayerModel(
    val id: String?,
    val email: String?,

    @SerializedName("first_name")
    val name: String?,

    val avatar: String?
)