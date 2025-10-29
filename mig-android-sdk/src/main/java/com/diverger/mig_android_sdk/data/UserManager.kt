package com.diverger.mig_android_sdk.data

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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

    suspend fun initializeUser(
        madridInGameUserData: MadridInGameUserData,
        accessToken: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            setAccessToken(accessToken)
            val response = apiService.getUserByEmail(madridInGameUserData.email, token = "Bearer $accessToken")
            if (response.data.isNotEmpty()) {
                val fetchedUser = response.data.first()
                val teams =
                    apiService.getTeamsByUser(fetchedUser.id, token = "Bearer $accessToken").data
                user = fetchedUser.copy(teams = teams)
                if (teams.isNotEmpty()) {
                    _selectedTeam.value = teams.first()
                }
                user?.let { user ->
                    val updateUserRequest = createUserRequestIfChanged(user, madridInGameUserData)
                    if (updateUserRequest != null) {
                        updateUser(user.id, updateUserRequest)
                    }
                }
                Result.success(Unit)
            } else {
                val dniValue = madridInGameUserData.dni
                val newUserResponse = apiService.createUser(
                    userRequest = UserRequest(
                        madridInGameUserData.email,
                        madridInGameUserData.userName,
                        dniValue,
                        name = madridInGameUserData.name,
                        lastName = madridInGameUserData.lastName,
                        phone = madridInGameUserData.phone
                    ),
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

    suspend fun updateUser(userId: String, dni: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val userDNIRequest = UserDNIRequest(dni = dni)
                val response = apiService.updateUserDNI(
                    userId = userId,
                    userDNIRequest,
                    token = "Bearer $accessToken"
                )
                if (response.data?.dni != null) {
                    user?.dni = response.data.dni
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error: No se pudo actualizar el DNI."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun updateUser(userId: String, userRequest: UserRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val response = apiService.updateUser(
                    userId = userId,
                    userRequest,
                    token = "Bearer $accessToken"
                )
                if (response.data != null) {
                    user?.dni = response.data.dni
                    user?.email = response.data.email
                    user?.username = response.data.username
                    user?.firstName = response.data.name
                    user?.lastName = response.data.lastName
                    user?.phone = response.data.phone
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error: No se pudo actualizar la información del usuario."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun createUserRequestIfChanged(
        serviceUser: User,
        madridInGameUserData: MadridInGameUserData
    ): UserRequest? {
        val updatedDni = if (serviceUser.dni.isNullOrBlank()) madridInGameUserData.dni else serviceUser.dni

        val updatedEmail = if (serviceUser.email != madridInGameUserData.email) madridInGameUserData.email else serviceUser.email
        val updatedUsername = if (serviceUser.username != madridInGameUserData.userName) madridInGameUserData.userName else serviceUser.username
        val updatedName = if (serviceUser.firstName != madridInGameUserData.name) madridInGameUserData.name else serviceUser.firstName
        val updatedLastName = if (serviceUser.lastName != madridInGameUserData.lastName) madridInGameUserData.lastName else serviceUser.lastName
        val updatedPhone = if (serviceUser.phone != madridInGameUserData.phone) madridInGameUserData.phone else serviceUser.phone

        val isDifferent = (updatedEmail != serviceUser.email) ||
                (updatedUsername != serviceUser.username) ||
                (updatedDni != serviceUser.dni) ||
                (updatedName != serviceUser.firstName) ||
                (updatedLastName != serviceUser.lastName) ||
                (updatedPhone != serviceUser.phone)

        return if (isDifferent) {
            UserRequest(
                email = updatedEmail,
                username = updatedUsername,
                dni = updatedDni,
                name = updatedName,
                lastName = updatedLastName,
                phone = updatedPhone
            )
        } else {
            null
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
            @Header("Authorization") token: String,
        ): UserResponse

        @GET("teams")
        suspend fun getTeamsByUser(
            @Query("filter[users][users_id][_eq]") userId: String,
            @Query("fields") fields: String = "id,name,description,picture,apply_membership,status,discord,users.roles.*,users.users_id.id,users.users_id.username,users.users_id.avatar,competitions.competitions_id,date_edited",
            @Header("Authorization") token: String,
        ): TeamResponse

        @POST("users")
        suspend fun createUser(
            @Body userRequest: UserRequest,
            @Header("Authorization") token: String,
        ): UserCreatedResponse

        @PATCH("users/{userId}")
        suspend fun updateUserDNI(
            @Path("userId") userId: String,
            @Body userRequest: UserDNIRequest,
            @Header("Authorization") token: String,
        ): SingleUserResponse

        @PATCH("users/{userId}")
        suspend fun updateUser(
            @Path("userId") userId: String,
            @Body userRequest: UserRequest,
            @Header("Authorization") token: String,
        ): SingleUserResponse
    }
}

data class UserRequest(
    val email: String,
    val username: String,
    val dni: String?,
    @SerializedName("first_name") val name: String?,
    @SerializedName("last_name") val lastName: String?,
    val phone: String?
)

data class UserDNIRequest(
    val dni: String,
)

data class SingleUserResponse(val data: UserResponseModel?)

data class UserResponseModel(
    val id: String,
    val email: String,
    val dni: String?,
    val username: String,
    @SerializedName("first_name") val name: String?,
    @SerializedName("last_name") val lastName: String?,
    val phone: String?
)

data class UserResponse(val data: List<User>)
data class UserCreatedResponse(val data: User?)
data class TeamResponse(val data: List<Team>)

data class User(
    val id: String,
    val status: String,
    var username: String,
    var email: String,
    var dni: String?,
    val token: String?,
    @SerializedName("first_name") var firstName: String?,
    @SerializedName("last_name") var lastName: String?,
    val avatar: String?,
    @SerializedName("reserves_allowed") val reservesAllowed: Int,
    var phone: String?,
    val trainings: List<Int>,
    @SerializedName("gaming_space_reserves") val gamingSpaceReserves: List<Int>,
    val invitations: List<Int>,
    var teams: List<Team> = emptyList(),
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
    val competitions: List<TeamCompetition>?,
)

data class TeamUser(
    @SerializedName("roles") val role: Role?,
    @SerializedName("users_id") val userId: UserId,
)

data class Role(
    val id: String,
    val name: String,
)

data class UserId(
    val id: String,
    val username: String,
    val avatar: String?,
)

data class TeamCompetition(
    @SerializedName("competitions_id") val id: String,
)

fun getUnsafeOkHttpClient(): OkHttpClient {
    return try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val sslSocketFactory = sslContext.socketFactory
        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

@Composable
fun UnsafeAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    error: Painter? = null,
) {
    val context = LocalContext.current

    val unsafeImageLoader = ImageLoader.Builder(context)
        .okHttpClient(getUnsafeOkHttpClient())
        .build()

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        imageLoader = unsafeImageLoader,
        contentScale = contentScale,
        placeholder = placeholder,
        error = error,
        onError = { throwable ->
            Log.e(
                "ImageLoad",
                "Error loading image",
                throwable.result.throwable
            )
        }
    )
}