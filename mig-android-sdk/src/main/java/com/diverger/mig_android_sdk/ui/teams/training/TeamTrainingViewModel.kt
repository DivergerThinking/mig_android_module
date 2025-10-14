package com.diverger.mig_android_sdk.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.TrainingApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeamTrainingViewModel : ViewModel() {
    private val _trainings = MutableStateFlow<List<EventModel>>(emptyList())
    val trainings: StateFlow<List<EventModel>> = _trainings

    private val _teamReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val teamReservations: StateFlow<List<Reservation>> = _teamReservations

    val _markedDates = MutableStateFlow<List<String>>(emptyList())
    val markedDates: StateFlow<List<String>> = _markedDates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val user = UserManager.getUser()
    private val teamId = UserManager.selectedTeam.value?.id

    init {
        if (teamId != null) {
            //fetchTeamReservations()
            //fetchTeamTrainings()
            observeSelectedTeam()
        } else {
            _errorMessage.value = "El usuario no pertenece a ningún equipo."
        }
    }

    private fun observeSelectedTeam() {
        viewModelScope.launch {
            UserManager.selectedTeam
                .collect { team ->
                    _isLoading.value = true
                    _errorMessage.value = null

                    if (team?.id != null) {
                        val result = TrainingApi.getTrainings(team.id)
                        result.onSuccess { trainings ->
                            _trainings.value = trainings
                            _isLoading.value = false
                        }.onFailure {
                            _isLoading.value = false
                            _errorMessage.value = "Error al obtener entrenamientos."
                        }
                    }
                }
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

    fun fetchTeamTrainings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = TrainingApi.getTrainings(teamId!!)
            result.onSuccess { trainings ->
                _trainings.value = trainings
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
                _errorMessage.value = "Error al obtener entrenamientos."
            }
        }
    }
}
