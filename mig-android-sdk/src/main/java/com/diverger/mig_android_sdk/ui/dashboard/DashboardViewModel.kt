package com.diverger.mig_android_sdk.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.TrainingApi
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.dateFromString
import com.diverger.mig_android_sdk.support.isDateOlderThanToday
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.filter

class DashboardViewModel : ViewModel() {
    private val _trainings = MutableStateFlow<List<EventModel>>(emptyList())
    val trainings: StateFlow<List<EventModel>> = _trainings

    private val _teamReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val teamReservations: StateFlow<List<Reservation>> = _teamReservations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun getData() {
        UserManager.getUser()?.id?.let { userId ->
            viewModelScope.launch {
                _isLoading.value = true

                val trainingsDeferred = async { TrainingApi.getTrainings() }
                val reservationsDeferred = async { ReservationApi.getReservations(userId) }

                val result = trainingsDeferred.await()
                result.onSuccess { trainings ->
                    _trainings.value =
                        trainings.filter { dateFromString(it.startDate)?.isDateOlderThanToday() == true }
                }.onFailure {
                    _errorMessage.value = "Error al obtener entrenamientos."
                }

                val reservationsResult = reservationsDeferred.await()
                reservationsResult.onSuccess { reservations ->
                    _teamReservations.value =
                        reservations.filter { dateFromString(it.date)?.isDateOlderThanToday() == true }
                }.onFailure {
                    _errorMessage.value = "Error al obtener entrenamientos."
                }

                _isLoading.value = false
            }
        }
    }
}