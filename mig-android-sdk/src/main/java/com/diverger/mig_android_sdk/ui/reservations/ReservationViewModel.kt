package com.diverger.mig_android_sdk.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.UserManager
import kotlinx.coroutines.flow.*
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
            try {
                _reservations.value = ReservationApi.getReservations(userId)
            } catch (e: Exception) {
                _reservations.value = emptyList()
            }
            _isLoading.value = false
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
}
