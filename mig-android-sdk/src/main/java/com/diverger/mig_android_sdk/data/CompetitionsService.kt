package com.diverger.mig_android_sdk.data

import com.google.gson.annotations.SerializedName

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class CompetitionResponse(
    val data: List<Competition>
)

data class CompetitionWrapper(val data: Competition)

data class Competition(
    val id: String,
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
    val teams: List<TeamWrapper>?
)

data class Game(
    val id: String,
    val name: String,
    val image: String?,
    val banner: String?
)

data class Split(
    val id: Int,
    val name: String,
    val tournaments: List<Tournament>
)

data class Tournament(
    val id: Int,
    val name: String,
    @SerializedName("date") val tournamentDate: String?,
    val link: String?
)

data class TeamWrapper(
    @SerializedName("teams_id") val team: Team
)

interface CompetitionsService {

    @GET("competitions")
    suspend fun getAllCompetitions(
        @Query("fields") fields: String = "*,teams.teams_id.name,teams.teams_id.picture,teams.teams_id.id,translations.*,game.*,splits.*,splits.tournaments.*",
        @Query("filter[start_date][_between]") yearRange: String?,
        @Header("Authorization") token: String
    ): CompetitionResponse

    @GET("competitions")
    suspend fun getCompetitionsByTeam(
        @Query("fields") fields: String = "*,teams.teams_id.name,teams.teams_id.picture,teams.teams_id.id,translations.*",
        @Query("filter[teams][teams_id][_eq]") teamId: String,
        @Query("filter[start_date][_between]") yearRange: String?,
        @Header("Authorization") token: String
    ): CompetitionResponse

    @GET("competitions/{id}")
    suspend fun getCompetitionById(
        @Path("id") competitionId: String,
        @Query("fields") fields: String = "*,teams.teams_id.name,teams.teams_id.picture,teams.teams_id.id,translations.*,game.*,splits.*,splits.tournaments.*",
        @Header("Authorization") token: String
    ): CompetitionWrapper
}

object CompetitionsApi {
    private const val BASE_URL = "https://premig.randomkesports.com/cms/items/"
    private const val TOKEN = "Bearer 8TZMs1jYI1xIts2uyUnE_MJrPQG9KHfY"

    val service: CompetitionsService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CompetitionsService::class.java)
    }

    suspend fun getAllCompetitions(year: Int? = null): List<Competition> {
        val yearRange = year?.let { "$it-01-01,$it-12-31" }
        return service.getAllCompetitions(yearRange = yearRange, token = TOKEN).data
    }

    suspend fun getCompetitionsByTeam(teamId: String, year: Int? = null): List<Competition> {
        val yearRange = year?.let { "$it-01-01,$it-12-31" }
        // TODO : Cambiar cuando haya competiciones para mi team
        //return service.getCompetitionsByTeam(teamId = teamId, yearRange = yearRange, token = TOKEN).data
        return service.getAllCompetitions(yearRange = yearRange, token = TOKEN).data
    }

    suspend fun getCompetitionById(competitionId: String): Competition {
        return service.getCompetitionById(competitionId, token = TOKEN).data
    }
}