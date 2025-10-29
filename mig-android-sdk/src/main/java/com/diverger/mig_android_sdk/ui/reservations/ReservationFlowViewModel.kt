package com.diverger.mig_android_sdk.ui.reservations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservationFlowViewModel : ViewModel() {

    // Estado del paso actual en el flujo de reserva
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

    private val _selectedSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val selectedSlots: StateFlow<List<GamingSpaceTime>> = _selectedSlots

    private val _selectedSpace = MutableStateFlow<Space?>(null)
    val selectedSpace: StateFlow<Space?> = _selectedSpace

    // Estado de carga para mostrar un indicador de progreso al crear la reserva
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    // 📌 **Fechas marcadas en el calendario**
    private val _markedDates = MutableStateFlow<List<MarkedDate>>(emptyList())
    val markedDates: StateFlow<List<MarkedDate>> = _markedDates

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
        loadCalendarData()
    }

    fun goToNextStep() {
        if (_currentStep.value < 2) _currentStep.value++
    }

    fun goToPreviousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            _isLoading.value = true

            val blockedDatesDeferred = async { BlockedDaysService.fetchBlockedDates() }
            val individualReservationsDeferred = async { fetchIndividualReservations() }
            val teamTrainingsDeferred = async { fetchTeamTrainings() }

            val blockedDatesResult = blockedDatesDeferred.await()
            val individualReservations = individualReservationsDeferred.await()
            val teamTrainings = teamTrainingsDeferred.await()

            val markedDatesList = mutableListOf<MarkedDate>()

            blockedDatesResult.onSuccess { dates ->
                _blockedDates.value = dates
                markedDatesList.addAll(dates.map { MarkedDate(it, isBlocked = true) })
            }

            markedDatesList.addAll(individualReservations)
            markedDatesList.addAll(teamTrainings)

            _markedDates.value = markedDatesList.distinctBy { it.date }
            calculateAvailableDates()
            _isLoading.value = false
        }
    }

    /**
     * 📌 **Obtiene reservas individuales del usuario**
     */
    private suspend fun fetchIndividualReservations(): List<MarkedDate> {
        val userId = UserManager.getUser()?.id ?: return emptyList()

        return try {
            val result = ReservationApi.getReservations(userId)
            result.getOrDefault(emptyList()).mapNotNull { reservation ->
                reservation.date?.let {
                    MarkedDate(it, isReservation = true)
                }
            }
        } catch (e: Exception) {
            Log.e("fetchIndividualReservations", "Error obteniendo reservas individuales: ${e.message}")
            emptyList()
        }
    }

    /**
     * 📌 **Obtiene entrenamientos del equipo**
     */
    private suspend fun fetchTeamTrainings(): List<MarkedDate> {
        val selectedTeam = UserManager.getSelectedTeam() ?: return emptyList()
        val result = ReservationApi.getReservationsByTeam(selectedTeam.id)
        return result.getOrDefault(emptyList()).mapNotNull { training ->
            training.date.let {
                MarkedDate(it, isTraining = true)
            }
        }
    }

    /**
     * 📌 **Calcula las fechas disponibles (sin bloqueos)**
     */
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

    /**
     * 📌 **Selecciona una fecha**
     */
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        val dayValue = calculateDayValue(date)
        fetchAvailableSlots(dayValue)
    }

    /**
     * 📌 **Obtiene el valor del día de la semana**
     */
    private fun calculateDayValue(dateString: String): Int {
        val date = dateFormatter.parse(dateString) ?: return 0
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_WEEK) - 1 // Lunes = 1, Domingo = 7
    }

    /**
     * 📌 **Clase para fechas marcadas en el calendario**
     */
    data class MarkedDate(
        val date: String,
        val isReservation: Boolean = false,
        val isTraining: Boolean = false,
        val isBlocked: Boolean = false
    )

    private fun fetchAvailableSlots(dayValue: Int) {
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

    private fun updateEnabledSlots() {
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

    fun cleanSlots() {
        _selectedSlots.value = emptyList()
        updateEnabledSlots()
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

    suspend fun createReservation(userId: String): Boolean {
        val date = _selectedDate.value
        val space = _selectedSpace.value
        val slots = _selectedSlots.value

        if (date == null || space == null || slots.isEmpty()) {
            Log.e("ReservationFlow", "Datos incompletos para crear la reserva")
            return false
        }

        val mappedTimes = slots.map { slot ->
            mapOf(
                "gaming_space_times_id" to GamingSpaceTimeId(id = slot.id)
            )
        }

        val reservation = ReservationWrapper(
            id = null,
            status = "active",
            slot = space.slots.first().id,
            date = try {
                dateFormatter.format(dateFormatter.parse(date))
            } catch (e: Exception) {
                Log.e("ReservationFlow", "Error al formatear la fecha: ${e.message}")
                return false
            },
            user = userId,
            team = null,
            training = null,
            qrImage = null,
            qrValue = null,
            timesContainer = mappedTimes,
            peripheralLoans = emptyList()
        )

        _isProcessing.value = true
        //_reservationSuccess.value = null

        return try {
            val result = ReservationApi.createReservation(reservation)
            if (result.isSuccess) {
                _reservationSuccess.value = true
                Log.i("ReservationFlow", "Reserva creada exitosamente")
                true
            } else {
                _reservationSuccess.value = false
                Log.e("ReservationFlow", "Error al crear la reserva")
                false
            }
        } catch (e: Exception) {
            _reservationSuccess.value = false
            Log.e("ReservationFlow", "Excepción al crear la reserva: ${e.message}")
            false
        } finally {
            _isProcessing.value = false
        }
    }
}