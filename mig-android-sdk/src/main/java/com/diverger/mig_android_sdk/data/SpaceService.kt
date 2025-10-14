package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.annotations.SerializedName

interface SpaceServiceApi {
    @GET("gaming_space")
    suspend fun getSpaces(
        @Query("fields") fields: String = "*,translations.*,slots.*",
        @Query("filter[group][_eq]") group: Boolean = false,
        @Header("Authorization") token: String
    ): SpaceResponse
}

object SpaceService {
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private val TOKEN = "Bearer ${UserManager.getAccessToken()}"

    private val service: SpaceServiceApi by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpaceServiceApi::class.java)
    }

    suspend fun fetchSpaces(): Result<List<Space>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getSpaces(token = TOKEN)
                val spaces = response.data.mapNotNull { item ->
                    item.translations.find { it.languagesCode == "es" }?.let { translation ->
                        Space(
                            id = item.id,
                            device = translation.device,
                            description = translation.description,
                            slots = item.slots
                        )
                    }
                }
                Result.success(spaces)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class Space(
    val id: Int,
    val device: String,
    val description: String,
    val slots: List<Slot>
)

data class Slot(
    val id: Int,
    val position: String,
    val space: Int
)

data class SlotReservation(
    val id: Int,
    val position: String,
    val space: SpaceItemReservation
)

data class SpaceResponse(
    val data: List<SpaceItem>
)

data class SpaceItem(
    val id: Int,
    val group: Boolean,
    val translations: List<Translation>,
    val slots: List<Slot>
)

data class SpaceItemReservation(
    val id: Int,
    val group: Boolean,
    val translations: List<Translation>,
)

data class Translation(
    val id: Int,
    @SerializedName("gaming_space_id") val gamingSpaceId: Int,
    @SerializedName("languages_code") val languagesCode: String,
    val device: String,
    val description: String
)
