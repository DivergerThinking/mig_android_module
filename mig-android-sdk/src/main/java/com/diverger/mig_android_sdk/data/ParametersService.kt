package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

data class AppParametersResponse(val data: AppParameters)

data class AppParameters(
    val id: Int,
    @SerializedName("reservesSingleLimit") val reservesSingleLimit: Int,
    @SerializedName("trainingsTeamLimit") val trainingsTeamLimit: Int,
    @SerializedName("teamNewsLimit") val teamNewsLimit: Int,
    @SerializedName("maxTeamPlayers") val maxTeamPlayers: Int
)

interface ParametersServiceApi {
    @GET("parameters")
    suspend fun getParameters(
        @Header("Authorization") token: String
    ): AppParametersResponse
}

object ParametersService {
    private val service: ParametersServiceApi by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(EnvironmentManager.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ParametersServiceApi::class.java)
    }

    suspend fun fetchParameters(): Result<AppParameters> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer ${UserManager.getAccessToken()}"
                val response = service.getParameters(token = token)
                Result.success(response.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
