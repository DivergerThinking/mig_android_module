package com.diverger.mig_android_sdk.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeamTrainingViewModel : ViewModel() {
    private val _teamReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val teamReservations: StateFlow<List<Reservation>> = _teamReservations

    val _markedDates = MutableStateFlow<List<String>>(emptyList())
    val markedDates: StateFlow<List<String>> = _markedDates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val user = UserManager.getUser()
    private val teamId = user?.teams?.firstOrNull()?.id

    init {
        if (teamId != null) {
            fetchTeamReservations()
        } else {
            _errorMessage.value = "El usuario no pertenece a ningún equipo."
        }
    }

    fun fetchTeamReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = ReservationApi.getReservationsByTeam(teamId!!)

            result.onSuccess { reservations ->
                _teamReservations.value = reservations
                _markedDates.value = reservations.map { it.date }
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
                _teamReservations.value = emptyList()
            }
        }
    }
}
