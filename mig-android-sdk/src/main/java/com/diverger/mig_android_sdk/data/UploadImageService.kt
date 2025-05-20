// UploadImageService.kt
package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

class UploadImageService {
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl(EnvironmentManager.getFilesBaseUrl())
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadApiService::class.java)
    }

    /**
     * Sube el array de bytes de la imagen y devuelve el ID generado por Directus.
     */
    suspend fun uploadImageSuspend(imageBytes: ByteArray, fileName: String): String =
        withContext(Dispatchers.IO) {
            val requestFile = imageBytes
                .toRequestBody("image/jpeg".toMediaType())
            val bodyPart = MultipartBody.Part
                .createFormData("file", fileName, requestFile)

            // Recupera token y lo añade como Bearer
            val token = UserManager.getAccessToken()
                ?: throw Exception("Token de acceso no disponible")
            val bearer = "Bearer $token"

            // Llamada a Retrofit con header
            val response: Response<UploadResponse> =
                api.uploadFile(bearer, bodyPart)

            if (response.isSuccessful) {
                response.body()?.data?.id
                    ?: throw Exception("No se devolvió ID de imagen")
            } else {
                throw Exception("Error al subir imagen: ${response.errorBody()?.string()}")
            }
        }

    private interface UploadApiService {
        @Multipart
        @POST("files")
        suspend fun uploadFile(
            @Header("Authorization") token: String,
            @Part file: MultipartBody.Part
        ): Response<UploadResponse>
    }

    private data class UploadResponse(val data: UploadData)
    private data class UploadData(val id: String)
}
