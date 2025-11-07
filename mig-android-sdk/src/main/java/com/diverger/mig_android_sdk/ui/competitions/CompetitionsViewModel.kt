package com.diverger.mig_android_sdk.ui.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.CompetitionsApi
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.getCurrentYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CompetitionsViewModel : ViewModel() {

    // 🔄 Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 🚨 Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions

    private val _selectedYear = MutableStateFlow<String>("${getCurrentYear()}")
    val selectedYear: StateFlow<String> = _selectedYear

    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears

    private val validCompetitions = arrayOf("esm", "junior", "stormCircuit",  "other")

    init {
        fetchCompetitions()
    }

    private fun fetchAvailableYears(competitions: List<Competition>) {
        _availableYears.value = getUniqueYears(competitions).map { it.toString() }
    }

    private fun fetchCompetitions(year: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val userTeamId = UserManager.getUser()?.teams?.firstOrNull()?.id

                val response = if (userTeamId != null) {
                    CompetitionsApi.getCompetitionsByTeam(userTeamId, year)
                } else {
                    CompetitionsApi.getAllCompetitions(year)
                }

                fetchAvailableYears(response)

                val filteredByYear = filterCompetitionsByYear(response, selectedYear.value.toInt())

                if (response.isNotEmpty()) {
                    _competitions.value = filteredByYear
                } else {
                    _competitions.value = emptyList()
                    _errorMessage.value = "No hay competiciones disponibles para este año."
                }


            } catch (e: Exception) {
                _competitions.value = emptyList()
                _errorMessage.value = "Error al obtener competiciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSelectedYear(year: String) {
        _selectedYear.value = year
        fetchCompetitions(year.toInt())
    }

    private fun parseDateSafe(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateStr)
        } catch (e: ParseException) {
            null
        }
    }

    private fun filterCompetitionsByYear(competitions: List<Competition>, year: Int): List<Competition> {
        return competitions.filter { competition ->
            parseDateSafe(competition.startDate)?.let { date ->
                Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) == year
            } == true && competition.game?.type in validCompetitions
        }
    }

    private fun getUniqueYears(competitions: List<Competition>): Set<Int> {
        val years = mutableSetOf<Int>()
        competitions.filter { it.game?.type in validCompetitions}.forEach { competition ->
            parseDateSafe(competition.startDate)?.let { date ->
                years.add(Calendar.getInstance().apply { time = date }.get(Calendar.YEAR))
            }
        }
        return years
    }

}