package com.diverger.mig_android_sdk.data

import android.util.Log
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TrainingService {
    @GET("trainings")
    suspend fun getAllTrainings(
        @Query("filter[team][_eq]") teamId: String?,
        @Query("filter[start_date][_gte]") startDateFrom: String = "\$NOW",
        @Query("sort[]") sort: String = "start_date",
        @Query("fields") fields: String = "id,start_date,time,players.users_id.id,players.users_id.avatar,players.users_id.email,players.users_id.first_name,type,reserves.*,reserves.team.name,reserves.team.picture,reserves.times.gaming_space_times_id.time,notes",
        @Header("Authorization") token: String,
    ): TrainingResponse

    @DELETE("trainings/{id}")
    suspend fun deleteTraining(
        @Path("id") trainingId: String,
        @Header("Authorization") token: String
    )

    @PATCH("trainings/{id}")
    suspend fun updateTraining(
        @Path("id") trainingId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Header("Authorization") token: String
    ): TrainingResponseModel

    @POST("trainings")
    suspend fun createTraining(
        @Body body: Map<String, @JvmSuppressWildcards Any>,
        @Header("Authorization") token: String
    ): TrainingResponseModel
}

object TrainingApi {
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private val TOKEN get() = "Bearer ${UserManager.getAccessToken()}"

    val service: TrainingService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrainingService::class.java)
    }

    suspend fun getTrainings(teamId: String? = null): Result<List<EventModel>> {
        return try {
            val response = service.getAllTrainings(teamId, token = TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTraining(request: TrainingRequest): Result<TrainingCreatedData> {
        return try {
            val body = mapOf<String, Any>(
                "status" to request.status,
                "type" to request.type,
                "start_date" to request.startDate,
                "time" to request.time,
                "team" to request.team,
                "notes" to request.notes,
                "players" to request.players,
                "reserves" to request.reserves
            )
            val response = service.createTraining(body, TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTraining(
        trainingId: String,
        type: String,
        startDate: String,
        time: String,
        notes: String,
        playerIds: List<String>
    ): Result<TrainingCreatedData> {
        return try {
            val playersMapped = playerIds.map { mapOf("users_id" to it) }
            val body = mapOf<String, Any>(
                "type" to type,
                "start_date" to startDate,
                "time" to time,
                "notes" to notes,
                "players" to playersMapped
            )
            val response = service.updateTraining(trainingId, body, TOKEN)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkReservesToTraining(trainingId: String, reserveIds: List<Int>): Result<Unit> {
        return try {
            val body = mapOf<String, Any>("reserves" to reserveIds)
            service.updateTraining(trainingId, body, TOKEN)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update ONLY the players array of a training.
     * Used for team edit flow (only players editable for now).
     */
    suspend fun updateTrainingPlayers(trainingId: String, playerIds: List<String>): Result<Unit> {
        return try {
            val playersMapped = playerIds.map { mapOf("users_id" to it) }
            val body = mapOf<String, Any>("players" to playersMapped)
            service.updateTraining(trainingId, body, TOKEN)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Trigger a Directus Flow to send a reservation email.
     * iOS equivalent: DirectusService.shared.triggerFlow(flowId:body:)
     */
    suspend fun triggerEmailFlow(isIndividual: Boolean, body: Map<String, String>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val flowId = if (isIndividual) getIndividualFlowId() else getTeamFlowId()
                val url = "${EnvironmentManager.getBaseServerUrl()}/cms/flows/trigger/$flowId"

                Log.d("TrainingApi", "Triggering email flow: $url")
                Log.d("TrainingApi", "Body: $body")

                val client = getUnsafeOkHttpClient()
                val jsonBody = org.json.JSONObject(body).toString()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonBody.toRequestBody(mediaType)

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                Log.d("TrainingApi", "Email flow response: ${response.code} ${response.message}")

                if (response.isSuccessful) {
                    Log.d("TrainingApi", "Email sent successfully")
                    Result.success(Unit)
                } else {
                    val errorBody = response.body?.string()
                    Log.e("TrainingApi", "Email flow failed: $errorBody")
                    Result.failure(Exception("Flow trigger failed: ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e("TrainingApi", "Error triggering email flow: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private fun getIndividualFlowId(): String {
        val isPro = EnvironmentManager.getBaseServerUrl().contains("webesports")
        return if (isPro) "4740cad9-f737-4eea-abf4-228b1d606e30" else "d0e0e727-d47f-48bd-92c9-85ba3046579f"
    }

    private fun getTeamFlowId(): String {
        val isPro = EnvironmentManager.getBaseServerUrl().contains("webesports")
        return if (isPro) "a9754b93-655a-488c-b43f-5501ae6c3b11" else "3a753e17-6004-461d-b548-ad3cebdb3111"
    }

    /**
     * Elimina un training y sus reserves asociadas (si es presencial).
     * Replica la lógica de iOS: primero borra el training, luego las reserves.
     */
    suspend fun deleteTraining(trainingId: String, reserveIds: List<Int>): Result<Unit> {
        return try {
            service.deleteTraining(trainingId, token = TOKEN)

            for (reserveId in reserveIds) {
                try {
                    ReservationApi.service.deleteReservation(reserveId, token = TOKEN)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Models
// ═══════════════════════════════════════════════════════════════

data class TrainingResponse(val data: List<EventModel>)

data class EventModel(
    val id: String?,
    val notes: String?,
    val players: List<PlayerUsersModel>?,
    val reserves: List<Reserve>?,
    @SerializedName("start_date") val startDate: String,
    val status: String,
    val time: String,
    val type: String,
    var teamName: String?
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
    val user: String?,
)

data class Time(
    @SerializedName("gaming_space_times_id") val gamingSpaceTimesID: GamingSpaceTimesID?,
)

data class GamingSpaceTimesID(
    val time: String?,
)

data class PlayerUsersModel(
    @SerializedName("users_id") val userId: PlayerModel?,
)

data class PlayerModel(
    val id: String?,
    val email: String?,

    @SerializedName("first_name")
    val name: String?,

    val avatar: String?,
)

// ═══════════════════════════════════════════════════════════════
// Training request/response models
// ═══════════════════════════════════════════════════════════════

data class TrainingRequest(
    val status: String,
    val type: String,
    @SerializedName("start_date") val startDate: String,
    val time: String,
    val team: String,
    val notes: String,
    val players: List<Map<String, String>>,
    val reserves: List<Int>
)

data class TrainingResponseModel(
    val data: TrainingCreatedData
)

data class TrainingCreatedData(
    val id: String?
)