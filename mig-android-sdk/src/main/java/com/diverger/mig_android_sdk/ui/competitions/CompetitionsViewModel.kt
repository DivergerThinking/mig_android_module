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
import java.util.Calendar

class CompetitionsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions

    private val _selectedYear = MutableStateFlow("${getCurrentYear()}")
    val selectedYear: StateFlow<String> = _selectedYear

    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears

    // Matches competition.type (the competition's own type, not game.type)
    private val validCompetitions = arrayOf("esm", "junior", "stormCircuit", "other")

    init {
        // Mirror iOS: build year list from 2024 to current year, then fetch current year
        _availableYears.value = initAvailableYears()
        fetchCompetitions(getCurrentYear())
    }

    // Mirrors iOS initAllSeasons() — hardcoded range starting 2024
    private fun initAvailableYears(): List<String> {
        val currentYear = getCurrentYear()
        return (2024..currentYear).map { it.toString() }.sortedDescending()
    }

    private fun fetchCompetitions(year: Int) {
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

                // Mirror iOS filterCompetitonsByType: filter by competition.type, not game.type
                val filtered = response.filter { it.type in validCompetitions }
                val sorted = filtered.sortedBy { it.game?.priority ?: Int.MAX_VALUE }

                if (sorted.isNotEmpty()) {
                    _competitions.value = sorted
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
}
