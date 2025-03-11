package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface NewsService {
    @GET("team_news")
    suspend fun getTeamNews(
        @Query("filter[team][_eq]") teamId: String,
        @Query("fields") fields: String = "id,status,title,body,image,team.*,date",
        @Header("Authorization") token: String
    ): NewsResponse
}

object NewsApi {
    private val TOKEN = "Bearer ${UserManager.getAccessToken()}"
    private val BASE_URL = EnvironmentManager.getBaseUrl()

    val service: NewsService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsService::class.java)
    }

    suspend fun getNews(teamId: String): List<NewsModel> {
        return service.getTeamNews(teamId, token = TOKEN).data
    }
}

data class NewsResponse(val data: List<NewsModel>)

data class NewsModel(
    val id: Int,
    val title: String?,
    val body: String?,
    val date: String?,
    val image: String?,
    val status: String?
)
