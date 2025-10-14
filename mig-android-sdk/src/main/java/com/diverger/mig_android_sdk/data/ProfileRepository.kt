// ProfileRepository.kt
package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

object ProfileRepository {
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getBaseUrl())
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProfileApi::class.java)
    }

    /**
     * Hace PATCH al usuario logeado para actualizar solo el campo "avatar".
     */
    suspend fun updateAvatar(avatarId: String) = withContext(Dispatchers.IO) {
        val user = UserManager.getUser()
            ?: throw Exception("Usuario no autenticado")
        val token = UserManager.getAccessToken()
            ?: throw Exception("Token de acceso no disponible")
        val body = mapOf("avatar" to avatarId)

        val response: Response<AvatarResponse> =
            api.patchUserAvatar(user.id, body, "Bearer $token")
        if (!response.isSuccessful) {
            throw Exception("Error actualizando avatar: ${response.errorBody()?.string()}")
        }
    }

    private interface ProfileApi {
        @PATCH("users/{id}")
        suspend fun patchUserAvatar(
            @Path("id") userId: String,
            @Body body: Map<String, String>,
            @Header("Authorization") token: String
        ): Response<AvatarResponse>
    }

    private data class AvatarResponse(val data: AvatarData)
    private data class AvatarData(val avatar: String?)
}
