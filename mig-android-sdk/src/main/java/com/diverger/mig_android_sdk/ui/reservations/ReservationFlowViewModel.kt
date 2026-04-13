package com.diverger.mig_android_sdk.ui.reservations

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.*
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.support.generateQrBitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservationFlowViewModel(
    val personalReservations: Boolean = true,
    private val teamReservationInfo: EventModel? = null,
    private val individualReservationInfo: Reservation? = null
) : ViewModel() {

    // ── Navigation ───────────────────────────────────────────
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    val totalSteps: Int
        get() = when {
            personalReservations -> personalStepCount
            isEditing && !personalReservations -> 1  // Edit team: only players
            isVirtualTeam -> 4   // Place → Players → Date → Time
            else -> 5            // Place → Players → Date → Space → Slot
        }

    /** Dynamic step count for individual based on add-on choices (like iOS personalStepCount) */
    private val personalStepCount: Int
        get() = when {
            isSelectedSpaceSimulator -> if (_wantsExtraSpace.value) 6 else 4
            else -> if (_wantsSimulator.value) 5 else 4
        }

    // ── Date ─────────────────────────────────────────────────
    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

    private val _initialSelectedDateMillis = MutableStateFlow<Long?>(null)
    val initialSelectedDateMillis: StateFlow<Long?> = _initialSelectedDateMillis

    // ── Slots (main reservation) ─────────────────────────────
    private val _availableSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val availableSlots: StateFlow<List<GamingSpaceTime>> = _availableSlots

    private val _selectedSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val selectedSlots: StateFlow<List<GamingSpaceTime>> = _selectedSlots

    private val _enabledSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val enabledSlots: StateFlow<List<GamingSpaceTime>> = _enabledSlots

    // ── Spaces ───────────────────────────────────────────────
    private val _availableSpaces = MutableStateFlow<List<Space>>(emptyList())
    val availableSpaces: StateFlow<List<Space>> = _availableSpaces

    private val _selectedSpace = MutableStateFlow<Space?>(null)
    val selectedSpace: StateFlow<Space?> = _selectedSpace

    // ── Calendar marks ───────────────────────────────────────
    private val _markedDates = MutableStateFlow<List<MarkedDate>>(emptyList())
    val markedDates: StateFlow<List<MarkedDate>> = _markedDates

    private val _blockedDates = MutableStateFlow<List<String>>(emptyList())
    val blockedDates: StateFlow<List<String>> = _blockedDates

    private val _availableDates = MutableStateFlow<List<String>>(emptyList())
    val availableDates: StateFlow<List<String>> = _availableDates

    // ── Team-specific ────────────────────────────────────────
    private val _selectedSpaceType = MutableStateFlow<String?>(null)
    val selectedSpaceType: StateFlow<String?> = _selectedSpaceType

    private val _reservationNotes = MutableStateFlow("")
    val reservationNotes: StateFlow<String> = _reservationNotes

    private val _selectedPlayers = MutableStateFlow<List<String>>(emptyList())
    val selectedPlayers: StateFlow<List<String>> = _selectedPlayers

    private val _teamPlayers = MutableStateFlow<List<TeamUser>>(emptyList())
    val teamPlayers: StateFlow<List<TeamUser>> = _teamPlayers

    private val _selectedTime = MutableStateFlow("")
    val selectedTime: StateFlow<String> = _selectedTime

    // ── Simulator add-on (individual only) ───────────────────
    private val _wantsSimulator = MutableStateFlow(false)
    val wantsSimulator: StateFlow<Boolean> = _wantsSimulator

    private val _simulatorSpace = MutableStateFlow<Space?>(null)
    val simulatorSpace: StateFlow<Space?> = _simulatorSpace

    private val _availableSimulatorSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val availableSimulatorSlots: StateFlow<List<GamingSpaceTime>> = _availableSimulatorSlots

    private val _simulatorSelectedSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val simulatorSelectedSlots: StateFlow<List<GamingSpaceTime>> = _simulatorSelectedSlots

    // ── Extra space add-on (individual only, when space IS simulator) ──
    private val _wantsExtraSpace = MutableStateFlow(false)
    val wantsExtraSpace: StateFlow<Boolean> = _wantsExtraSpace

    private val _extraSpace = MutableStateFlow<Space?>(null)
    val extraSpace: StateFlow<Space?> = _extraSpace

    private val _availableExtraSpaceSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val availableExtraSpaceSlots: StateFlow<List<GamingSpaceTime>> = _availableExtraSpaceSlots

    private val _extraSpaceSelectedSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val extraSpaceSelectedSlots: StateFlow<List<GamingSpaceTime>> = _extraSpaceSelectedSlots

    private val _enabledExtraSpaceSlots = MutableStateFlow<List<GamingSpaceTime>>(emptyList())
    val enabledExtraSpaceSlots: StateFlow<List<GamingSpaceTime>> = _enabledExtraSpaceSlots

    // ── Slot occupancy ───────────────────────────────────────
    private val _isLoadingOccupancy = MutableStateFlow(false)
    val isLoadingOccupancy: StateFlow<Boolean> = _isLoadingOccupancy

    private val _occupiedTimeIds = MutableStateFlow<Set<Int>>(emptySet())
    val occupiedTimeIds: StateFlow<Set<Int>> = _occupiedTimeIds

    private val _isLoadingSimulatorOccupancy = MutableStateFlow(false)
    val isLoadingSimulatorOccupancy: StateFlow<Boolean> = _isLoadingSimulatorOccupancy

    private val _occupiedSimulatorTimeIds = MutableStateFlow<Set<Int>>(emptySet())
    val occupiedSimulatorTimeIds: StateFlow<Set<Int>> = _occupiedSimulatorTimeIds

    private val _isLoadingExtraSpaceOccupancy = MutableStateFlow(false)
    val isLoadingExtraSpaceOccupancy: StateFlow<Boolean> = _isLoadingExtraSpaceOccupancy

    private val _occupiedExtraSpaceTimeIds = MutableStateFlow<Set<Int>>(emptySet())
    val occupiedExtraSpaceTimeIds: StateFlow<Set<Int>> = _occupiedExtraSpaceTimeIds

    // Per-slot occupancy maps: slotId → Set<timeId> — used to pick a free slot at booking time
    private var slotOccupancyMap: Map<Int, Set<Int>> = emptyMap()
    private var simulatorSlotOccupancyMap: Map<Int, Set<Int>> = emptyMap()
    private var extraSpaceSlotOccupancyMap: Map<Int, Set<Int>> = emptyMap()

    // ── Loading / Processing ─────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _reservationSuccess = MutableStateFlow(false)
    val reservationSuccess: StateFlow<Boolean> = _reservationSuccess

    private val _showLegendPopup = MutableStateFlow(false)
    val showLegendPopup: StateFlow<Boolean> = _showLegendPopup

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Computed ──────────────────────────────────────────────
    val isEditing: Boolean
        get() = teamReservationInfo != null || individualReservationInfo != null

    val isVirtualTeam: Boolean
        get() = _selectedSpaceType.value?.lowercase() == "virtual"

    val isSelectedSpaceSimulator: Boolean
        get() = _selectedSpace.value?.device?.lowercase()?.contains("simulador") == true

    val nonSimulatorSpaces: List<Space>
        get() = _availableSpaces.value.filter { !it.device.lowercase().contains("simulador") }

    /** Whether the slot step should go to ask-addon instead of confirming */
    val shouldGoToAskAddon: Boolean
        get() = personalReservations && !isEditing

    init {
        loadCalendarData()
        loadTeamPlayers()
        populateFromExistingInformation()
    }

    // ═══════════════════════════════════════════════════════════
    // Summary tokens (like iOS)
    // ═══════════════════════════════════════════════════════════

    fun getSummaryTokens(): List<String> {
        val tokens = mutableListOf<String>()
        val step = _currentStep.value
        if (!personalReservations && step > 0) _selectedSpaceType.value?.let { tokens.add(it) }
        if (!personalReservations && step > 1 && _selectedPlayers.value.isNotEmpty()) tokens.add(_selectedPlayers.value.joinToString(", "))
        if (step > (if (personalReservations) 0 else 2)) _selectedDate.value?.let { tokens.add(it) }
        if (step > (if (personalReservations) 1 else 3)) _selectedSpace.value?.let { tokens.add(it.device) }
        return tokens
    }

    fun getSummaryTitle(): String {
        return if (isEditing && !personalReservations) "Editar jugadores" else "Reservar espacio"
    }

    // ═══════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════

    fun goToNextStep() { if (_currentStep.value < totalSteps - 1) _currentStep.value++ }
    fun goToPreviousStep() { if (_currentStep.value > 0) _currentStep.value-- }

    // ═══════════════════════════════════════════════════════════
    // Setters
    // ═══════════════════════════════════════════════════════════

    fun setSelectedSpaceType(type: String) { _selectedSpaceType.value = type }
    fun setReservationNotes(notes: String) { _reservationNotes.value = notes }
    fun setSelectedTime(time: String) { _selectedTime.value = time }
    fun toggleLegendPopup() { _showLegendPopup.value = !_showLegendPopup.value }

    // ═══════════════════════════════════════════════════════════
    // Players (team)
    // ═══════════════════════════════════════════════════════════

    private fun loadTeamPlayers() {
        _teamPlayers.value = UserManager.getSelectedTeam()?.users ?: emptyList()
    }

    fun addPlayer(username: String) {
        if (!_selectedPlayers.value.contains(username)) _selectedPlayers.value = _selectedPlayers.value + username
    }

    fun removePlayer(username: String) {
        _selectedPlayers.value = _selectedPlayers.value.filter { it != username }
    }

    private fun resolvePlayerIds(): List<String> {
        return _selectedPlayers.value.mapNotNull { username ->
            _teamPlayers.value.firstOrNull { it.userId.username == username }?.userId?.id
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Space type mapping
    // ═══════════════════════════════════════════════════════════

    companion object {
        fun spaceTypeToUI(backendValue: String?): String? = when (backendValue?.lowercase()) {
            "centre" -> "E-Sports Center"; "virtual" -> "Virtual"; else -> null
        }
        fun spaceTypeToBackend(uiValue: String?): String = when (uiValue?.lowercase()) {
            "e-sports center" -> "centre"; "virtual" -> "virtual"; else -> "centre"
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Pre-populate from existing info (editing)
    // ═══════════════════════════════════════════════════════════

    private fun populateFromExistingInformation() {
        teamReservationInfo?.let { populateFromTeamReservation(it) }
        individualReservationInfo?.let { populateFromIndividualReservation(it) }
    }

    private fun populateFromTeamReservation(event: EventModel) {
        _reservationNotes.value = event.notes ?: ""
        _selectedSpaceType.value = spaceTypeToUI(event.type)
        _selectedTime.value = event.time
        _selectedDate.value = event.startDate
        try { dateFormatter.parse(event.startDate)?.let { _initialSelectedDateMillis.value = it.time } } catch (_: Exception) {}

        _selectedPlayers.value = event.players?.mapNotNull { playerModel ->
            val playerId = playerModel.userId?.id ?: return@mapNotNull null
            _teamPlayers.value.firstOrNull { it.userId.id == playerId }?.userId?.username
        } ?: emptyList()

        if (event.type.lowercase() != "virtual") {
            val reserveSlotId = event.reserves?.firstOrNull()?.slot ?: return
            fetchAvailableSpaces {
                _selectedSpace.value = _availableSpaces.value.firstOrNull { space -> space.slots.any { it.id == reserveSlotId } }
                val dayValue = calculateDayValue(_selectedDate.value ?: return@fetchAvailableSpaces)
                fetchAvailableSlots(dayValue) {
                    val reserveTimes = event.reserves?.firstOrNull()?.times?.mapNotNull { it.gamingSpaceTimesID?.time }?.toSet() ?: emptySet()
                    _selectedSlots.value = _availableSlots.value.filter { it.time in reserveTimes }
                    updateEnabledSlots()
                }
            }
        }
    }

    private fun populateFromIndividualReservation(reservation: Reservation) {
        _selectedDate.value = reservation.date
        try { dateFormatter.parse(reservation.date)?.let { _initialSelectedDateMillis.value = it.time } } catch (_: Exception) {}

        fetchAvailableSpaces {
            _selectedSpace.value = _availableSpaces.value.firstOrNull { space -> space.slots.any { it.id == reservation.slot.id } }
            val dayValue = calculateDayValue(reservation.date)
            fetchAvailableSlots(dayValue) {
                val reserveTimes = reservation.times.map { it.time }.toSet()
                _selectedSlots.value = _availableSlots.value.filter { it.time in reserveTimes }
                updateEnabledSlots()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Calendar data
    // ═══════════════════════════════════════════════════════════

    private fun loadCalendarData() {
        viewModelScope.launch {
            _isLoading.value = true
            val blocked = async { BlockedDaysService.fetchBlockedDates() }
            val individual = async { fetchIndividualReservations() }
            val team = async { fetchTeamTrainings() }

            val allMarks = mutableListOf<MarkedDate>()
            blocked.await().onSuccess { dates -> _blockedDates.value = dates; allMarks.addAll(dates.map { MarkedDate(it, isBlocked = true) }) }
            allMarks.addAll(individual.await())
            allMarks.addAll(team.await())

            _markedDates.value = allMarks.distinctBy { it.date }
            calculateAvailableDates()
            _isLoading.value = false
        }
    }

    private suspend fun fetchIndividualReservations(): List<MarkedDate> {
        val userId = UserManager.getUser()?.id ?: return emptyList()
        return try {
            ReservationApi.getReservations(userId).getOrDefault(emptyList()).mapNotNull { it.date?.let { d -> MarkedDate(d, isReservation = true) } }
        } catch (e: Exception) { Log.e("FlowVM", "Error individual: ${e.message}"); emptyList() }
    }

    private suspend fun fetchTeamTrainings(): List<MarkedDate> {
        val team = UserManager.getSelectedTeam() ?: return emptyList()
        return try {
            TrainingApi.getTrainings(team.id).getOrDefault(emptyList()).map { MarkedDate(it.startDate, isTraining = true) }
        } catch (e: Exception) { Log.e("FlowVM", "Error trainings: ${e.message}"); emptyList() }
    }

    private fun calculateAvailableDates() {
        val today = Calendar.getInstance()
        val dates = mutableListOf<String>()
        for (i in 0..30) {
            val f = dateFormatter.format(today.time)
            if (!_blockedDates.value.contains(f)) dates.add(f)
            today.add(Calendar.DAY_OF_YEAR, 1)
        }
        _availableDates.value = dates
    }

    // ═══════════════════════════════════════════════════════════
    // Date selection
    // ═══════════════════════════════════════════════════════════

    fun setSelectedDate(date: String) {
        if (_markedDates.value.any { it.date == date && it.isBlocked }) return
        _selectedDate.value = date
        fetchAvailableSlots(calculateDayValue(date))
    }

    private fun calculateDayValue(dateString: String): Int {
        val date = dateFormatter.parse(dateString) ?: return 0
        val cal = Calendar.getInstance().apply { time = date }
        val weekday = cal.get(Calendar.DAY_OF_WEEK)
        return if (weekday == Calendar.SUNDAY) 7 else weekday - 1
    }

    // ═══════════════════════════════════════════════════════════
    // Slots (main)
    // ═══════════════════════════════════════════════════════════

    private fun fetchAvailableSlots(dayValue: Int, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = WeekTimeService.fetchWeekTimeByDay(dayValue)
            result.onSuccess { slots ->
                _availableSlots.value = if (isSelectedSpaceSimulator) slots else slots.filter { !it.time.endsWith(":30") }
                val space = _selectedSpace.value
                val date = _selectedDate.value
                if (space != null && date != null) {
                    _isLoadingOccupancy.value = true
                    fetchOccupiedTimes(space, date) { onComplete?.invoke() }
                } else {
                    updateEnabledSlots()
                    onComplete?.invoke()
                }
            }.onFailure { Log.e("FlowVM", "Error slots: ${it.message}"); onComplete?.invoke() }
        }
    }

    private fun fetchOccupiedTimes(space: Space, date: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = ReservationApi.getReservationsBySlots(space.slots.map { it.id }, date)
            result.onSuccess { entries ->
                val perSlot = mutableMapOf<Int, MutableSet<Int>>()
                val slotsPerTime = mutableMapOf<Int, MutableSet<Int>>()
                for (entry in entries) {
                    for (timeId in entry.timeIds) {
                        perSlot.getOrPut(entry.slot.id) { mutableSetOf() }.add(timeId)
                        slotsPerTime.getOrPut(timeId) { mutableSetOf() }.add(entry.slot.id)
                    }
                }
                slotOccupancyMap = perSlot
                _occupiedTimeIds.value = slotsPerTime.entries
                    .filter { it.value.size >= space.slots.size }
                    .map { it.key }.toSet()
            }.onFailure { Log.e("FlowVM", "Error occupancy: ${it.message}") }
            _isLoadingOccupancy.value = false
            updateEnabledSlots()
            onComplete?.invoke()
        }
    }

    private fun updateEnabledSlots() {
        val occupied = _occupiedTimeIds.value
        val notOccupied = _availableSlots.value.filter { it.id !in occupied }
        val selected = _selectedSlots.value
        if (selected.isEmpty()) { _enabledSlots.value = notOccupied; return }
        val maxSlots = if (isSelectedSpaceSimulator) 1 else if (personalReservations) 3 else 2
        val sorted = selected.sortedBy { it.value }
        val minVal = sorted.first().value; val maxVal = sorted.last().value
        _enabledSlots.value = notOccupied.filter { slot ->
            (slot.value in minVal..(maxVal + 1) && slot.value <= minVal + (maxSlots - 1)) || selected.any { it.id == slot.id }
        }
    }

    fun toggleSlotSelection(slot: GamingSpaceTime) {
        val current = _selectedSlots.value.toMutableList()
        if (current.any { it.id == slot.id }) current.removeAll { it.id == slot.id } else current.add(slot)
        _selectedSlots.value = current; updateEnabledSlots()
    }

    fun cleanSlots() { _selectedSlots.value = emptyList(); updateEnabledSlots() }

    // ═══════════════════════════════════════════════════════════
    // Simulator add-on
    // ═══════════════════════════════════════════════════════════

    fun setWantsSimulator(value: Boolean) { _wantsSimulator.value = value }

    fun findSimulatorSpace() {
        _simulatorSpace.value = _availableSpaces.value.firstOrNull { it.device.lowercase().contains("simulador") }
    }

    fun fetchSimulatorSlots(onComplete: (() -> Unit)? = null) {
        val dayValue = calculateDayValue(_selectedDate.value ?: return)
        viewModelScope.launch {
            val result = WeekTimeService.fetchWeekTimeByDay(dayValue)
            result.onSuccess { slots ->
                _availableSimulatorSlots.value = slots.sortedBy { it.time }
                val space = _simulatorSpace.value
                val date = _selectedDate.value
                if (space != null && date != null) {
                    _isLoadingSimulatorOccupancy.value = true
                    fetchOccupiedSimulatorTimes(space, date) { onComplete?.invoke() }
                    return@onSuccess
                }
            }.onFailure { Log.e("FlowVM", "Error simulator slots: ${it.message}") }
            onComplete?.invoke()
        }
    }

    private fun fetchOccupiedSimulatorTimes(space: Space, date: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = ReservationApi.getReservationsBySlots(space.slots.map { it.id }, date)
            result.onSuccess { entries ->
                val perSlot = mutableMapOf<Int, MutableSet<Int>>()
                val slotsPerTime = mutableMapOf<Int, MutableSet<Int>>()
                for (entry in entries) {
                    for (timeId in entry.timeIds) {
                        perSlot.getOrPut(entry.slot.id) { mutableSetOf() }.add(timeId)
                        slotsPerTime.getOrPut(timeId) { mutableSetOf() }.add(entry.slot.id)
                    }
                }
                simulatorSlotOccupancyMap = perSlot
                _occupiedSimulatorTimeIds.value = slotsPerTime.entries
                    .filter { it.value.size >= space.slots.size }
                    .map { it.key }.toSet()
            }.onFailure { Log.e("FlowVM", "Error simulator occupancy: ${it.message}") }
            _isLoadingSimulatorOccupancy.value = false
            onComplete?.invoke()
        }
    }

    fun toggleSimulatorSlotSelection(slot: GamingSpaceTime) {
        if (_simulatorSelectedSlots.value.any { it.id == slot.id }) {
            _simulatorSelectedSlots.value = emptyList()
        } else {
            _simulatorSelectedSlots.value = listOf(slot) // Max 1
        }
    }

    fun resetSimulatorSelection() {
        _wantsSimulator.value = false; _simulatorSelectedSlots.value = emptyList(); _availableSimulatorSlots.value = emptyList()
        _occupiedSimulatorTimeIds.value = emptySet(); _isLoadingSimulatorOccupancy.value = false; simulatorSlotOccupancyMap = emptyMap()
    }

    fun isSimulatorSlotBlockedByMain(slot: GamingSpaceTime): Boolean {
        val blockedHours = _selectedSlots.value.mapNotNull { hourFromTime(it.time) }.toSet()
        return hourFromTime(slot.time)?.let { blockedHours.contains(it) } ?: false
    }

    // ═══════════════════════════════════════════════════════════
    // Extra space add-on
    // ═══════════════════════════════════════════════════════════

    fun setWantsExtraSpace(value: Boolean) { _wantsExtraSpace.value = value }

    fun selectExtraSpace(space: Space) {
        if (_extraSpace.value?.id == space.id) {
            _extraSpace.value = null; _availableExtraSpaceSlots.value = emptyList()
            _extraSpaceSelectedSlots.value = emptyList(); _enabledExtraSpaceSlots.value = emptyList()
        } else {
            _extraSpace.value = space; _availableExtraSpaceSlots.value = emptyList()
            _extraSpaceSelectedSlots.value = emptyList(); _enabledExtraSpaceSlots.value = emptyList()
        }
    }

    fun fetchExtraSpaceSlots(onComplete: (() -> Unit)? = null) {
        val dayValue = calculateDayValue(_selectedDate.value ?: return)
        viewModelScope.launch {
            val result = WeekTimeService.fetchWeekTimeByDay(dayValue)
            result.onSuccess { slots ->
                _availableExtraSpaceSlots.value = slots.filter { !it.time.endsWith(":30") }
                val space = _extraSpace.value
                val date = _selectedDate.value
                if (space != null && date != null) {
                    _isLoadingExtraSpaceOccupancy.value = true
                    fetchOccupiedExtraSpaceTimes(space, date) { onComplete?.invoke() }
                    return@onSuccess
                }
                updateEnabledExtraSpaceSlots()
            }.onFailure { Log.e("FlowVM", "Error extra slots: ${it.message}") }
            onComplete?.invoke()
        }
    }

    private fun fetchOccupiedExtraSpaceTimes(space: Space, date: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = ReservationApi.getReservationsBySlots(space.slots.map { it.id }, date)
            result.onSuccess { entries ->
                val perSlot = mutableMapOf<Int, MutableSet<Int>>()
                val slotsPerTime = mutableMapOf<Int, MutableSet<Int>>()
                for (entry in entries) {
                    for (timeId in entry.timeIds) {
                        perSlot.getOrPut(entry.slot.id) { mutableSetOf() }.add(timeId)
                        slotsPerTime.getOrPut(timeId) { mutableSetOf() }.add(entry.slot.id)
                    }
                }
                extraSpaceSlotOccupancyMap = perSlot
                _occupiedExtraSpaceTimeIds.value = slotsPerTime.entries
                    .filter { it.value.size >= space.slots.size }
                    .map { it.key }.toSet()
            }.onFailure { Log.e("FlowVM", "Error extra space occupancy: ${it.message}") }
            _isLoadingExtraSpaceOccupancy.value = false
            updateEnabledExtraSpaceSlots()
            onComplete?.invoke()
        }
    }

    fun toggleExtraSpaceSlotSelection(slot: GamingSpaceTime) {
        val current = _extraSpaceSelectedSlots.value.toMutableList()
        if (current.any { it.id == slot.id }) current.removeAll { it.id == slot.id } else current.add(slot)
        _extraSpaceSelectedSlots.value = current; updateEnabledExtraSpaceSlots()
    }

    private fun updateEnabledExtraSpaceSlots() {
        val occupied = _occupiedExtraSpaceTimeIds.value
        val notOccupied = _availableExtraSpaceSlots.value.filter { it.id !in occupied }
        val selected = _extraSpaceSelectedSlots.value
        if (selected.isEmpty()) { _enabledExtraSpaceSlots.value = notOccupied; return }
        val sorted = selected.sortedBy { it.value }
        val minVal = sorted.first().value; val maxVal = sorted.last().value
        _enabledExtraSpaceSlots.value = notOccupied.filter { slot ->
            (slot.value in minVal..(maxVal + 1) && slot.value <= minVal + 2) || selected.any { it.id == slot.id }
        }
    }

    fun resetExtraSpaceSelection() {
        _wantsExtraSpace.value = false; _extraSpace.value = null
        _extraSpaceSelectedSlots.value = emptyList(); _availableExtraSpaceSlots.value = emptyList(); _enabledExtraSpaceSlots.value = emptyList()
        _occupiedExtraSpaceTimeIds.value = emptySet(); _isLoadingExtraSpaceOccupancy.value = false; extraSpaceSlotOccupancyMap = emptyMap()
    }

    fun isExtraSpaceSlotBlockedBySimulator(slot: GamingSpaceTime): Boolean {
        val blockedHours = _selectedSlots.value.mapNotNull { hourFromTime(it.time) }.toSet()
        return hourFromTime(slot.time)?.let { blockedHours.contains(it) } ?: false
    }

    private fun hourFromTime(time: String): Int? = time.split(":").firstOrNull()?.toIntOrNull()

    // ═══════════════════════════════════════════════════════════
    // Spaces
    // ═══════════════════════════════════════════════════════════

    fun fetchAvailableSpaces(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = SpaceService.fetchSpaces()
            result.onSuccess { spaces ->
                _availableSpaces.value = if (!personalReservations) spaces.filter { !it.device.lowercase().contains("simulador") } else spaces
            }.onFailure { Log.e("FlowVM", "Error spaces: ${it.message}") }
            onComplete?.invoke()
        }
    }

    fun selectSpace(space: Space) {
        if (_selectedSpace.value?.id == space.id) { _selectedSpace.value = null }
        else {
            _selectedSpace.value = space
            _availableSlots.value = emptyList(); _selectedSlots.value = emptyList(); _enabledSlots.value = emptyList()
            _occupiedTimeIds.value = emptySet(); _isLoadingOccupancy.value = false; slotOccupancyMap = emptyMap()
        }
    }

    fun cleanSpace() { _selectedSpace.value = null }

    // ═══════════════════════════════════════════════════════════
    // Create individual reservation + add-on
    // ═══════════════════════════════════════════════════════════

    /** Returns the first slot ID in the space that has no conflict with the given times. */
    private fun findFreeSlot(space: Space, occupancyMap: Map<Int, Set<Int>>, times: List<GamingSpaceTime>): Int {
        val selectedTimeIds = times.map { it.id }.toSet()
        for (slot in space.slots) {
            val bookedTimeIds = occupancyMap[slot.id] ?: emptySet()
            if (bookedTimeIds.none { it in selectedTimeIds }) return slot.id
        }
        return space.slots.first().id
    }

    suspend fun createReservation(userId: String, withLogo: Bitmap?): Boolean {
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val slots = _selectedSlots.value; if (slots.isEmpty()) return false

        val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }

        val reservation = ReservationWrapper(
            id = null, status = "active", slot = findFreeSlot(space, slotOccupancyMap, slots),
            date = formattedDate, user = userId, team = null, training = null,
            qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = emptyList()
        )

        _isProcessing.value = true
        return try {
            val result = ReservationApi.createReservation(reservation)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.qrValue != null && response.id != null) {
                    val qrBitmap = generateQrBitmap(response.qrValue, logoBitmap = withLogo)
                    if (qrBitmap != null) {
                        val qrImage = UploadImageService().uploadQrBitmap(qrBitmap, "${response.qrValue}.jpg")
                        val updated = reservation.copy(id = response.id, qrImage = qrImage, qrValue = response.qrValue)
                        val updateResult = ReservationApi.updateReservation(updated)
                        if (updateResult.isSuccess) {
                            // Create add-on if needed
                            val addonSuccess = createAddonIfNeeded(userId, formattedDate, withLogo)
                            // Send email
                            sendIndividualReservationEmail(formattedDate, space.device, slots)
                            _reservationSuccess.value = addonSuccess; addonSuccess
                        } else { ReservationApi.deleteReservation(response.id); false }
                    } else { ReservationApi.deleteReservation(response.id); false }
                } else { response?.id?.let { ReservationApi.deleteReservation(it) }; false }
            } else false
        } catch (e: Exception) { Log.e("FlowVM", "Error creating reservation: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    /** Creates simulator or extra space add-on reservation if the user opted in */
    private suspend fun createAddonIfNeeded(userId: String, date: String, withLogo: Bitmap?): Boolean {
        // Simulator add-on
        if (_wantsSimulator.value && _simulatorSelectedSlots.value.isNotEmpty()) {
            val simSpace = _simulatorSpace.value ?: return true
            val slotId = findFreeSlot(simSpace, simulatorSlotOccupancyMap, _simulatorSelectedSlots.value)
            return createAddonReservation(userId, date, slotId, _simulatorSelectedSlots.value, withLogo)
        }
        // Extra space add-on
        if (_wantsExtraSpace.value && _extraSpaceSelectedSlots.value.isNotEmpty()) {
            val extra = _extraSpace.value ?: return true
            val slotId = findFreeSlot(extra, extraSpaceSlotOccupancyMap, _extraSpaceSelectedSlots.value)
            return createAddonReservation(userId, date, slotId, _extraSpaceSelectedSlots.value, withLogo)
        }
        return true
    }

    private suspend fun createAddonReservation(userId: String, date: String, slotId: Int, times: List<GamingSpaceTime>, withLogo: Bitmap?): Boolean {
        val mappedTimes = times.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }
        val wrapper = ReservationWrapper(
            id = null, status = "active", slot = slotId,
            date = date, user = userId, team = null, training = null,
            qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = emptyList()
        )
        return try {
            val result = ReservationApi.createReservation(wrapper)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.qrValue != null && response.id != null) {
                    val qr = generateQrBitmap(response.qrValue, logoBitmap = withLogo)
                    if (qr != null) {
                        val qrImage = UploadImageService().uploadQrBitmap(qr, "${response.qrValue}.jpg")
                        ReservationApi.updateReservation(wrapper.copy(id = response.id, qrImage = qrImage, qrValue = response.qrValue))
                    }
                }
                true
            } else false
        } catch (e: Exception) { Log.e("FlowVM", "Error addon: ${e.message}"); false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update individual reservation (editing)
    // ═══════════════════════════════════════════════════════════

    suspend fun updateIndividualReservation(): Boolean {
        val reservationId = individualReservationInfo?.id ?: return false
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val slots = _selectedSlots.value; if (slots.isEmpty()) return false

        val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }

        val wrapper = ReservationWrapper(
            id = reservationId, status = "active", slot = space.slots.first().id,
            date = formattedDate, user = null, team = null, training = null,
            qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = null
        )
        _isProcessing.value = true
        return try {
            val result = ReservationApi.updateReservation(wrapper)
            _reservationSuccess.value = result.isSuccess; result.isSuccess
        } catch (e: Exception) { Log.e("FlowVM", "Error updating individual: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Create team training (centre) — full flow
    // ═══════════════════════════════════════════════════════════

    suspend fun createTeamReservation(withLogo: Bitmap?): Boolean {
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val teamId = UserManager.getSelectedTeam()?.id ?: return false
        val slots = _selectedSlots.value; if (slots.isEmpty()) return false

        val playerIds = resolvePlayerIds()
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
        val timeString = slots.sortedBy { it.value }.firstOrNull()?.time ?: _selectedTime.value
        val typeValue = spaceTypeToBackend(_selectedSpaceType.value)

        _isProcessing.value = true
        return try {
            val trainingResult = TrainingApi.createTraining(TrainingRequest(
                status = "active", type = typeValue, startDate = formattedDate,
                time = timeString, team = teamId, notes = _reservationNotes.value,
                players = playerIds.map { mapOf("users_id" to it) }, reserves = emptyList()
            ))
            val trainingId = trainingResult.getOrNull()?.id ?: return false
            val reserveIds = mutableListOf<Int>()
            val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }

            for (playerId in playerIds) {
                val reservation = ReservationWrapper(
                    id = null, status = "active", slot = space.slots.first().id,
                    date = formattedDate, user = playerId, team = teamId, training = trainingId,
                    qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = emptyList()
                )
                val reserveResult = ReservationApi.createReservation(reservation)
                val reserveResponse = reserveResult.getOrNull() ?: continue
                reserveResponse.id?.let { reserveIds.add(it) }
                if (reserveResponse.qrValue != null && reserveResponse.id != null) {
                    val qr = generateQrBitmap(reserveResponse.qrValue, logoBitmap = withLogo)
                    if (qr != null) {
                        val qrImage = UploadImageService().uploadQrBitmap(qr, "${reserveResponse.qrValue}.jpg")
                        ReservationApi.updateReservation(reservation.copy(id = reserveResponse.id, qrImage = qrImage, qrValue = reserveResponse.qrValue))
                    }
                }
            }
            if (reserveIds.isEmpty()) return false
            TrainingApi.linkReservesToTraining(trainingId, reserveIds)
            // Send email to each player
            sendTeamReservationEmails(formattedDate, space.device, slots)
            _reservationSuccess.value = true; true
        } catch (e: Exception) { Log.e("FlowVM", "Error team reservation: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update team training — ONLY players for now
    // Future: uncomment to also update date, space, slots, notes
    // ═══════════════════════════════════════════════════════════

    suspend fun updateCenterTeamReservation(): Boolean {
        val trainingId = teamReservationInfo?.id ?: return false
        val playerIds = resolvePlayerIds()
        _isProcessing.value = true
        return try {
            // Only update players
            val result = TrainingApi.updateTrainingPlayers(trainingId, playerIds)
            _reservationSuccess.value = result.isSuccess; result.isSuccess

            // Future: full update
            // val reserveId = teamReservationInfo?.reserves?.firstOrNull()?.id ?: return false
            // val date = _selectedDate.value ?: return false
            // val space = _selectedSpace.value ?: return false
            // val slots = _selectedSlots.value; if (slots.isEmpty()) return false
            // val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
            // val timeString = slots.sortedBy { it.value }.firstOrNull()?.time ?: _selectedTime.value
            // val typeValue = spaceTypeToBackend(_selectedSpaceType.value)
            // val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }
            // val reserveWrapper = ReservationWrapper(id = reserveId, status = "active", slot = space.slots.first().id,
            //     date = formattedDate, user = null, team = null, training = null,
            //     qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = null)
            // ReservationApi.updateReservation(reserveWrapper)
            // TrainingApi.updateTraining(trainingId, typeValue, formattedDate, timeString, _reservationNotes.value, playerIds)
        } catch (e: Exception) { Log.e("FlowVM", "Error updating centre: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Create virtual team training
    // ═══════════════════════════════════════════════════════════

    suspend fun createVirtualTeamReservation(): Boolean {
        val date = _selectedDate.value ?: return false
        val teamId = UserManager.getSelectedTeam()?.id ?: return false
        if (_selectedTime.value.isEmpty()) return false

        val playerIds = resolvePlayerIds()
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }

        _isProcessing.value = true
        return try {
            val result = TrainingApi.createTraining(TrainingRequest(
                status = "active", type = "virtual", startDate = formattedDate,
                time = _selectedTime.value, team = teamId, notes = _reservationNotes.value,
                players = playerIds.map { mapOf("users_id" to it) }, reserves = emptyList()
            ))
            if (result.isSuccess) {
                // Send email to each player
                sendVirtualTrainingEmails(formattedDate, _selectedTime.value)
            }
            _reservationSuccess.value = result.isSuccess; result.isSuccess
        } catch (e: Exception) { Log.e("FlowVM", "Error virtual: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update virtual team training — ONLY players for now
    // Future: uncomment to also update date, time, notes
    // ═══════════════════════════════════════════════════════════

    suspend fun updateVirtualTeamReservation(): Boolean {
        val trainingId = teamReservationInfo?.id ?: return false
        val playerIds = resolvePlayerIds()
        _isProcessing.value = true
        return try {
            val result = TrainingApi.updateTrainingPlayers(trainingId, playerIds)
            _reservationSuccess.value = result.isSuccess; result.isSuccess

            // Future: full update
            // val date = _selectedDate.value ?: return false
            // if (_selectedTime.value.isEmpty()) return false
            // val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
            // val result = TrainingApi.updateTraining(trainingId, "virtual", formattedDate, _selectedTime.value, _reservationNotes.value, playerIds)
        } catch (e: Exception) { Log.e("FlowVM", "Error updating virtual: ${e.message}"); false }
        finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Email sending via Directus Flows
    // ═══════════════════════════════════════════════════════════

    private fun sendIndividualReservationEmail(date: String, device: String, times: List<GamingSpaceTime>) {
        val user = UserManager.getUser() ?: return
        Log.d("FlowVM", ">>> User: ${user?.id}, email: '${user?.email}'")
        if (user == null) { Log.w("FlowVM", ">>> User is NULL, aborting email"); return }
        val email = user.email; if (email.isBlank()) return
        if (email.isBlank()) { Log.w("FlowVM", ">>> Email is BLANK, aborting email"); return }
        Log.d("FlowVM", ">>> Sending email to: $email")
        val sortedTimes = times.sortedBy { it.value }.joinToString(" - ") { it.time }
        val extraBlock = buildExtraBlock(date)

        viewModelScope.launch {
            Log.d("FlowVM", ">>> About to call triggerEmailFlow")

            try {
                val result = TrainingApi.triggerEmailFlow(
                    isIndividual = true,
                    body = mapOf(
                        "email" to email,
                        "first_name" to (user.firstName ?: ""),
                        "date" to date,
                        "times" to sortedTimes,
                        "device" to device,
                        "extra_block" to extraBlock
                    )
                )
                Log.d("FlowVM", ">>> triggerEmailFlow result: $result")

            } catch (e: Exception) {
                Log.e("FlowVM", "Error email: ${e.message}")
            }
        }
    }

    /** Send email to each player after creating a centre team training (like iOS sendTeamReservationEmail per player) */
    private fun sendTeamReservationEmails(date: String, device: String, times: List<GamingSpaceTime>) {
        val sortedTimes = times.sortedBy { it.value }.joinToString(" - ") { it.time }

        _selectedPlayers.value.forEach { username ->
            val teamUser = _teamPlayers.value.firstOrNull { it.userId.username == username } ?: return@forEach
            val email = teamUser.userId.email ?: return@forEach
            if (email.isBlank()) return@forEach

            viewModelScope.launch {
                try {
                    TrainingApi.triggerEmailFlow(
                        isIndividual = false,
                        body = mapOf(
                            "email" to email,
                            "first_name" to username,
                            "date" to date,
                            "times" to sortedTimes,
                            "device" to device
                        )
                    )
                } catch (e: Exception) { Log.e("FlowVM", "Error team email for $username: ${e.message}") }
            }
        }
    }

    /** Send email to each player after creating a virtual team training (like iOS sendVirtualTrainingEmail per player) */
    private fun sendVirtualTrainingEmails(date: String, time: String) {
        _selectedPlayers.value.forEach { username ->
            val teamUser = _teamPlayers.value.firstOrNull { it.userId.username == username } ?: return@forEach
            val email = teamUser.userId.email ?: return@forEach
            if (email.isBlank()) return@forEach

            viewModelScope.launch {
                try {
                    TrainingApi.triggerEmailFlow(
                        isIndividual = false,
                        body = mapOf(
                            "email" to email,
                            "first_name" to username,
                            "date" to date,
                            "times" to time,
                            "device" to "Virtual"
                        )
                    )
                } catch (e: Exception) { Log.e("FlowVM", "Error virtual email for $username: ${e.message}") }
            }
        }
    }

    private fun buildExtraBlock(date: String): String {
        if (_wantsSimulator.value && _simulatorSelectedSlots.value.isNotEmpty()) {
            val device = _simulatorSpace.value?.device ?: return ""
            val times = _simulatorSelectedSlots.value.sortedBy { it.value }.joinToString(" - ") { it.time }
            return "<p><strong><u>Reserva extra incluida:</u></strong><br><strong>Fecha:</strong> $date<br><strong>Hora:</strong> $times<br><strong>Dispositivo:</strong> $device</p>"
        }
        if (_wantsExtraSpace.value && _extraSpaceSelectedSlots.value.isNotEmpty()) {
            val device = _extraSpace.value?.device ?: return ""
            val times = _extraSpaceSelectedSlots.value.sortedBy { it.value }.joinToString(" - ") { it.time }
            return "<p><strong><u>Reserva extra incluida:</u></strong><br><strong>Fecha:</strong> $date<br><strong>Hora:</strong> $times<br><strong>Dispositivo:</strong> $device</p>"
        }
        return ""
    }

    // ═══════════════════════════════════════════════════════════
    // Data classes
    // ═══════════════════════════════════════════════════════════

    data class MarkedDate(
        val date: String,
        val isReservation: Boolean = false,
        val isTraining: Boolean = false,
        val isBlocked: Boolean = false,
    )
}