package com.diverger.mig_android_sdk.ui.competitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.CompetitionsApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompetitionsViewModel : ViewModel() {

    // 🔄 Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 🚨 Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions

    private val _selectedYear = MutableStateFlow<String>("")
    val selectedYear: StateFlow<String> = _selectedYear

    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears

    init {
        fetchAvailableYears()
        fetchCompetitions()
    }

    fun fetchAvailableYears() {
        val currentYear = 2024
        _availableYears.value = (currentYear..2029).map { it.toString() }
    }

    fun fetchCompetitions(year: Int? = null) {
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

                if (response.isNotEmpty()) {
                    _competitions.value = response
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