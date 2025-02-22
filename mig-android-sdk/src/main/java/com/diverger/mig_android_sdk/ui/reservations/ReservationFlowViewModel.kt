package com.diverger.mig_android_sdk.ui.reservations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservationFlowViewModel : ViewModel() {

    // Estado del paso actual en el flujo de reserva
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    // Datos seleccionados en el flujo
    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate: StateFlow<Date?> = _selectedDate

    private val _selectedSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val selectedSlots: StateFlow<List<GamingSpaceTime>> = _selectedSlots

    private val _selectedSpace = MutableStateFlow<Space?>(null)
    val selectedSpace: StateFlow<Space?> = _selectedSpace

    // Datos cargados desde el backend
    private val _blockedDates = MutableStateFlow<List<String>>(emptyList())
    val blockedDates: StateFlow<List<String>> = _blockedDates

    private val _availableDates = MutableStateFlow<List<String>>(emptyList())
    val availableDates: StateFlow<List<String>> = _availableDates

    private val _availableSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val availableSlots: StateFlow<List<GamingSpaceTime>> = _availableSlots

    private val _enabledSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val enabledSlots: StateFlow<List<GamingSpaceTime>> = _enabledSlots

    private val _availableSpaces = MutableStateFlow<List<Space>>(emptyList())
    val availableSpaces: StateFlow<List<Space>> = _availableSpaces

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _reservationSuccess = MutableStateFlow(false)
    val reservationSuccess: StateFlow<Boolean> = _reservationSuccess

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchBlockedDates()
    }

    fun goToNextStep() {
        if (_currentStep.value < 2) _currentStep.value++
    }

    fun goToPreviousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    fun fetchBlockedDates() {
        viewModelScope.launch {
            val result = BlockedDaysService.fetchBlockedDates()
            result.onSuccess { dates ->
                _blockedDates.value = dates
                calculateAvailableDates()
            }.onFailure { error ->
                Log.e("BlockedDaysService", "Error al obtener fechas bloqueadas: ${error.message}")
            }
        }
    }

    private fun calculateAvailableDates() {
        val today = Calendar.getInstance()
        val dates = mutableListOf<String>()

        for (i in 0..30) {
            val date = today.time
            val formattedDate = dateFormatter.format(date)

            if (!_blockedDates.value.contains(formattedDate)) {
                dates.add(formattedDate)
            }

            today.add(Calendar.DAY_OF_YEAR, 1)
        }

        _availableDates.value = dates
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)

        _selectedDate.value?.let {
            val dayValue = calculateDayValue(it)
            fetchAvailableSlots(dayValue)
        }
    }

    private fun calculateDayValue(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val weekday = calendar.get(Calendar.DAY_OF_WEEK) // Domingo es 1
        return weekday - 1 // Convertir al formato (Lunes = 1, Domingo = 7)
    }

    fun fetchAvailableSlots(dayValue: Int) {
        viewModelScope.launch {
            val result = WeekTimeService.fetchWeekTimeByDay(dayValue)
            result.onSuccess { slots ->
                _availableSlots.value = slots
                updateEnabledSlots()
            }.onFailure { error ->
                Log.e("WeekTimeService", "Error obteniendo horarios: ${error.message}")
            }
        }
    }

    fun updateEnabledSlots() {
        val selected = _selectedSlots.value

        if (selected.isEmpty()) {
            _enabledSlots.value = _availableSlots.value
            return
        }

        val sortedSelected = selected.sortedBy { it.value }
        val minValue = sortedSelected.first().value
        val maxValue = sortedSelected.last().value

        _enabledSlots.value = _availableSlots.value.filter { slot ->
            (slot.value in minValue..(maxValue + 1) && slot.value <= minValue + 2)
                    || selected.any { selectedSlot -> selectedSlot.id == slot.id }
        }
    }

    fun toggleSlotSelection(slot: GamingSpaceTime) {
        val selected = _selectedSlots.value.toMutableList()

        if (selected.contains(slot)) {
            selected.remove(slot)
        } else {
            selected.add(slot)
        }

        _selectedSlots.value = selected
        updateEnabledSlots()
    }

    fun fetchAvailableSpaces() {
        viewModelScope.launch {
            val result = SpaceService.fetchSpaces()
            result.onSuccess { spaces ->
                _availableSpaces.value = spaces
            }.onFailure { error ->
                Log.e("SpaceService", "Error obteniendo espacios: ${error.message}")
            }
        }
    }

    fun selectSpace(space: Space) {
        _selectedSpace.value = if (_selectedSpace.value?.id == space.id) null else space
    }

    fun createReservation(userId: String) {
        val date = _selectedDate.value
        val space = _selectedSpace.value
        val slots = _selectedSlots.value

        if (date == null || space == null || slots.isEmpty()) {
            Log.e("ReservationFlow", "Datos incompletos para crear la reserva")
            return
        }

        val mappedTimes = slots.map { mapOf("gaming_space_times_id" to it) }

        val reservation = Reservation(
            id = null,
            status = "active",
            slot = space.slots.first(),
            date = dateFormatter.format(date),
            user = userId,
            team = null,
            training = null,
            qrImage = null,
            qrValue = null,
            timesContainer = mappedTimes,
            peripheralLoans = emptyList()
        )

        _isLoading.value = true

        viewModelScope.launch {
            val result = ReservationApi.createReservation(reservation)
            result.onSuccess {
                _reservationSuccess.value = true
                Log.i("ReservationFlow", "Reserva creada exitosamente")
            }.onFailure { error ->
                _reservationSuccess.value = false
                Log.e("ReservationFlow", "Error al crear la reserva: ${error.message}")
            }

            _isLoading.value = false
        }
    }
}