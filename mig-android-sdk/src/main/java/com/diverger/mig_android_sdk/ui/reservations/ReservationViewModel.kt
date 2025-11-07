package com.diverger.mig_android_sdk.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun fetchReservations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ReservationApi.getReservations(userId)
            result.onSuccess { reservations ->
                _reservations.value = reservations
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
                _reservations.value = emptyList()
            }
            //_isLoading.value = false
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            viewModelScope.launch {
                _isLoading.value = true
                val result = runCatching { ReservationApi.deleteReservation(id) }

                result.onSuccess {
                    _isLoading.value = false
                    _toastMessage.value = "Reserva cancelada con éxito"
                    UserManager.getUser()?.id.let {
                        fetchReservations(UserManager.getUser()!!.id)
                    }
                }.onFailure {
                    _isLoading.value = false
                    _toastMessage.value = "Error al cancelar la reserva"
                }
            }
        }
    }

    fun updateUserDNI(userId: String, dni: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = runCatching {
                UserManager.updateUser(userId, dni = dni)
            }

            result.onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }

            _isLoading.value = false
        }
    }

    fun userCanBook() : Boolean {
        val reservationCount = reservations.value.count()
        return reservationCount < userReservationLimit()
    }

    fun userHasDNI(): Boolean {
        val user = UserManager.getUser()
        return user?.dni?.isNotBlank() ?: false
    }

    fun userIsValidated(): Boolean {
        val user = UserManager.getUser()
        return (userHasDNI() && user?.status?.lowercase() == "published")
    }

    private fun userReservationLimit(): Int {
        val user = UserManager.getUser()
        return if (userIsValidated()) {
            (user?.reservesAllowed ?: 1)
        } else {
            1
        }
    }



}
