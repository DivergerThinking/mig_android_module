package com.diverger.mig_android_sdk.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.BlockedDaysService
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.TrainingApi
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.StringResourcesProvider
import com.diverger.mig_android_sdk.support.dateFromString
import com.diverger.mig_android_sdk.support.isDateOlderThanToday
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val stringResourcesProvider: StringResourcesProvider) :
    ViewModel() {
    private val _trainings = MutableStateFlow<List<EventModel>>(emptyList())
    val trainings: StateFlow<List<EventModel>> = _trainings

    private val _teamReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val teamReservations: StateFlow<List<Reservation>> = _teamReservations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val _markedDates = MutableStateFlow<List<String>>(emptyList())
    val markedDates: StateFlow<List<String>> = _markedDates

    fun getData() {
        UserManager.getUser()?.id?.let { userId ->
            viewModelScope.launch {
                _isLoading.value = true

                val trainingsDeferred = async { getTrainings() }
                val reservationsDeferred = async { ReservationApi.getReservations(userId) }

                val result = trainingsDeferred.await()
                result.onSuccess { trainings ->
                    _trainings.value =
                        trainings.filter { dateFromString(it.startDate)?.isDateOlderThanToday() == true }
                }.onFailure {
                    _errorMessage.value =
                        stringResourcesProvider.getString(R.string.error_obtaining_trainings)
                }

                val reservationsResult = reservationsDeferred.await()
                reservationsResult.onSuccess { reservations ->
                    _teamReservations.value =
                        reservations.filter { dateFromString(it.date)?.isDateOlderThanToday() == true }
                }.onFailure {
                    _errorMessage.value =
                        stringResourcesProvider.getString(R.string.error_obtaining_trainings)
                }

                _isLoading.value = false
            }
        }
    }

    suspend fun getTrainings(): Result<List<EventModel>> {
        var trainings = mutableListOf<EventModel>()
        UserManager.getUser()?.teams?.forEach { team ->
            val result = TrainingApi.getTrainings(team.id)
            result.onSuccess {
                it.forEach { it.teamName = team.name }
                trainings.addAll(it)
            }
            result.onFailure {
                Result.failure<List<EventModel>>(it)
            }
        }

        return Result.success(trainings.filter { dateFromString(it.startDate)?.isDateOlderThanToday() == true })
    }

    private fun getMarkedDates() {
        viewModelScope.launch {
            val blockedDatesDeferred = async { BlockedDaysService.fetchBlockedDates() }

            val blockedDatesResult = blockedDatesDeferred.await()

            blockedDatesResult.onSuccess { dates ->
                _markedDates.value = dates
            }
        }
    }
}