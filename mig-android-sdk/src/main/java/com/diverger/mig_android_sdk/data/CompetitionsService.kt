package com.diverger.mig_android_sdk.data

import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.google.gson.annotations.SerializedName

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 📌 **Respuesta del API**
data class CompetitionResponse(
    val data: List<Competition>
)

data class CompetitionWrapper(val data: Competition)

// 📌 **Modelo de Competición**
data class Competition(
    val id: String,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("date_updated") val dateUpdated: String?,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_sign_date") val endSignDate: String?,
    @SerializedName("start_sign_date") val startSignDate: String?,
    val title: String,
    val rules: String?,
    val overview: String?,
    val details: String?,
    val contact: String?,
    val splits: List<Split>?,
    val game: Game?,
    val type: String?,
    @SerializedName("pdf_file") val pdfFile: String?
)

// 📌 **Modelo de Juego**
data class Game(
    val id: String,
    val name: String,
    val image: String?,
    val banner: String?,
    val type: String,
    val description: String?,
    val priority: Int?
)

// 📌 **Modelo de Split**
data class Split(
    val id: Int,
    val name: String,
    val tournaments: List<Tournament>?
)

// 📌 **Inscripción a torneo**
data class TournamentInscriptionRequest(
    val alias: String,
    val status: String = "published",
    @SerializedName("id_user") val idUser: String?,
    @SerializedName("id_tournament") val idTournament: Int,
    @SerializedName("id_team") val idTeam: String?,
    val email: String?
)

// 📌 **Modelo de Torneo**
data class Tournament(
    val id: Int,
    val name: String,
    @SerializedName("date") val tournamentDate: String?,
    val link: String?,
    val split: Int?,
    val status: String?
)

interface CompetitionsService {

    @GET("competitions")
    suspend fun getAllCompetitions(
        @Query("fields") fields: String = "*,game.*, splits.*, splits.tournaments.*",
        @Query(value = "filter[season][_eq]", encoded = true) season: String?,
        @Header("Authorization") token: String
    ): CompetitionResponse

    @GET("competitions")
    suspend fun getCompetitionsByTeam(
        @Query("fields") fields: String = "*,teams.teams_id.name,teams.teams_id.picture,teams.teams_id.id,translations.*",
        @Query("filter[teams][teams_id][_eq]") teamId: String,
        @Query("filter[start_date][_between]") yearRange: String?,
        @Header("Authorization") token: String
    ): CompetitionResponse

    @POST("tournament_inscriptions")
    suspend fun createTournamentInscription(
        @Body body: TournamentInscriptionRequest,
        @Header("Authorization") token: String
    ): Unit

    @GET("competitions/{id}")
    suspend fun getCompetitionById(
        @Path("id") competitionId: String,
        @Query("fields") fields: String = "*,teams.teams_id.name,teams.teams_id.picture,teams.teams_id.id,translations.*,game.*,splits.*,splits.tournaments.*",
        @Header("Authorization") token: String
    ): CompetitionWrapper
}

object CompetitionsApi {
    private val BASE_URL = EnvironmentManager.getBaseUrl()
    private val TOKEN = "Bearer ${UserManager.getAccessToken()}"

    val service: CompetitionsService by lazy {
        Retrofit.Builder()
            .client(getUnsafeOkHttpClient())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CompetitionsService::class.java)
    }

    suspend fun getAllCompetitions(year: Int? = null): List<Competition> {
        val season = year?.toString()
        return service.getAllCompetitions(
            fields = "*,game.*,splits.*,splits.tournaments.*",
            season = season,
            token = TOKEN
        ).data
    }

    suspend fun getCompetitionsByTeam(teamId: String, year: Int? = null): List<Competition> {
        val season = year?.toString()
        // TODO: Filter by team when team competitions are available
        return service.getAllCompetitions(season = season, token = TOKEN).data
    }

    suspend fun getCompetitionById(competitionId: String): Competition {
        return service.getCompetitionById(competitionId, token = TOKEN).data
    }

    suspend fun createTournamentInscription(request: TournamentInscriptionRequest) {
        service.createTournamentInscription(request, token = TOKEN)
    }
}