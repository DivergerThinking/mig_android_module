package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import okhttp3.OkHttpClient
import retrofit2.http.Body
import retrofit2.http.POST
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object UserManager {
    private var user: User? = null
    private val _selectedTeam = MutableStateFlow<Team?>(null)
    val selectedTeam: StateFlow<Team?> = _selectedTeam
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private var accessToken: String? = null

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun initializeUser(email: String, userName: String, dni: String, accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            setAccessToken(accessToken)
            val response = apiService.getUserByEmail(email, token = "Bearer $accessToken")
            if (response.data.isNotEmpty()) {
                val fetchedUser = response.data.first()
                val teams = apiService.getTeamsByUser(fetchedUser.id, token = "Bearer $accessToken").data
                user = fetchedUser.copy(teams = teams)
                if (teams.isNotEmpty()) {
                    _selectedTeam.value = teams.first()
                }
                Result.success(Unit)
            } else {
                val newUserResponse = apiService.createUser(
                    userRequest = UserRequest(email, userName, dni),
                    token = "Bearer $accessToken"
                )

                if (newUserResponse.data == null) {
                    return@withContext Result.failure(Exception("Error: No se pudo crear el usuario. Respuesta vacía."))
                }

                val newUser = newUserResponse.data
                user = newUser

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUser(): User? = user

    fun getSelectedTeam(): Team? = _selectedTeam.value

    fun setSelectedTeam(team: Team) {
       _selectedTeam.value = team
    }

    fun setAccessToken(token: String) {
        accessToken = token
    }

    fun getAccessToken(): String? = accessToken

    interface ApiService {
        @GET("users")
        suspend fun getUserByEmail(
            @Query("filter[email][_eq]") email: String,
            @Query("fields") fields: String = "*,teams.teams_id.id,teams.teams_id.name",
            @Header("Authorization") token: String
        ): UserResponse

        @GET("teams")
        suspend fun getTeamsByUser(
            @Query("filter[users][users_id][_eq]") userId: String,
            @Query("fields") fields: String = "id,name,description,picture,apply_membership,status,discord,users.roles.*,users.users_id.id,users.users_id.username,users.users_id.avatar,competitions.competitions_id,date_edited",
            @Header("Authorization") token: String
        ): TeamResponse

        @POST("users")
        suspend fun createUser(
            @Body userRequest: UserRequest,
            @Header("Authorization") token: String
        ): UserCreatedResponse
    }
}

data class UserRequest(
    val email: String,
    val username: String,
    val dni: String
)

data class UserResponse(val data: List<User>)
data class UserCreatedResponse(val data: User?)
data class TeamResponse(val data: List<Team>)

data class User(
    val id: String,
    val status: String,
    val username: String,
    val email: String,
    val dni: String?,
    val token: String?,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val avatar: String?,
    @SerializedName("reserves_allowed") val reservesAllowed: Int,
    val phone: String?,
    val trainings: List<Int>,
    @SerializedName("gaming_space_reserves") val gamingSpaceReserves: List<Int>,
    val invitations: List<Int>,
    var teams: List<Team> = emptyList()
)

data class Team(
    val id: String,
    val name: String?,
    val description: String?,
    val picture: String?,
    @SerializedName("apply_membership") val applyMembership: Boolean?,
    val status: String?,
    val discord: String?,
    @SerializedName("date_edited") val dateEdited: String?,
    val users: List<TeamUser>?,
    val competitions: List<TeamCompetition>?
)

data class TeamUser(
    @SerializedName("roles") val role: Role?,
    @SerializedName("users_id") val userId: UserId
)

data class Role(
    val id: String,
    val name: String
)

data class UserId(
    val id: String,
    val username: String,
    val avatar: String?
)

data class TeamCompetition(
    @SerializedName("competitions_id") val id: String
)

fun getUnsafeOkHttpClient(): OkHttpClient {
    return try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}