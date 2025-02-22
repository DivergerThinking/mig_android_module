package com.diverger.mig_android_sdk.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
            try {
                ReservationApi.deleteReservation(id)
                _reservations.value = _reservations.value.filter { it.id != id }
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }
}
