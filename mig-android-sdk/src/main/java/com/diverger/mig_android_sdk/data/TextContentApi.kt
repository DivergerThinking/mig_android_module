package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TextContentService {
    @GET("text_content_mig")
    suspend fun getTextContent(
        @Query("fields") fields: String = "key,en,es",
        @Query("limit") limit: Int = -1,
        @Header("Authorization") token: String
    ): TextContentResponse
}

object TextContentApi {
    private val service: TextContentService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(EnvironmentManager.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TextContentService::class.java)
    }

    suspend fun fetchTextContent(): List<TextContentItem> {
        return service.getTextContent(token = "Bearer ${UserManager.getAccessToken()}").data
    }
}
