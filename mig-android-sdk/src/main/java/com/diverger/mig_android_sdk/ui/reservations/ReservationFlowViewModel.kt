package com.diverger.mig_android_sdk.ui.reservations

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.data.*
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
            personalReservations -> 3
            isVirtualTeam -> 4   // Place → Players → Date → Time
            else -> 5            // Place → Players → Date → Space → Slot
        }

    // ── Date ─────────────────────────────────────────────────
    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

    private val _initialSelectedDateMillis = MutableStateFlow<Long?>(null)
    val initialSelectedDateMillis: StateFlow<Long?> = _initialSelectedDateMillis

    // ── Slots ────────────────────────────────────────────────
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

    // ── Loading / Processing ─────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _reservationSuccess = MutableStateFlow(false)
    val reservationSuccess: StateFlow<Boolean> = _reservationSuccess

    // ── Legend popup ──────────────────────────────────────────
    private val _showLegendPopup = MutableStateFlow(false)
    val showLegendPopup: StateFlow<Boolean> = _showLegendPopup

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Editing check ────────────────────────────────────────
    val isEditing: Boolean
        get() = teamReservationInfo != null || individualReservationInfo != null

    val isVirtualTeam: Boolean
        get() = _selectedSpaceType.value?.lowercase() == "virtual"

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

        if (!personalReservations && step > 0) {
            _selectedSpaceType.value?.let { tokens.add(it) }
        }
        if (!personalReservations && step > 1 && _selectedPlayers.value.isNotEmpty()) {
            tokens.add(_selectedPlayers.value.joinToString(", "))
        }
        val dateThreshold = if (personalReservations) 0 else 2
        if (step > dateThreshold) {
            _selectedDate.value?.let { tokens.add(it) }
        }
        val spaceThreshold = if (personalReservations) 1 else 3
        if (step > spaceThreshold) {
            _selectedSpace.value?.let { tokens.add(it.device) }
        }
        return tokens
    }

    // ═══════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════

    fun goToNextStep() {
        if (_currentStep.value < totalSteps - 1) _currentStep.value++
    }

    fun goToPreviousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    // ═══════════════════════════════════════════════════════════
    // Space type (team)
    // ═══════════════════════════════════════════════════════════

    fun setSelectedSpaceType(type: String) {
        _selectedSpaceType.value = type
    }

    fun setReservationNotes(notes: String) {
        _reservationNotes.value = notes
    }

    fun setSelectedTime(time: String) {
        _selectedTime.value = time
    }

    fun toggleLegendPopup() {
        _showLegendPopup.value = !_showLegendPopup.value
    }

    // ═══════════════════════════════════════════════════════════
    // Players (team)
    // ═══════════════════════════════════════════════════════════

    private fun loadTeamPlayers() {
        _teamPlayers.value = UserManager.getSelectedTeam()?.users ?: emptyList()
    }

    fun addPlayer(username: String) {
        if (!_selectedPlayers.value.contains(username)) {
            _selectedPlayers.value = _selectedPlayers.value + username
        }
    }

    fun removePlayer(username: String) {
        _selectedPlayers.value = _selectedPlayers.value.filter { it != username }
    }

    /**
     * Resolve selected usernames back to user IDs for the API.
     * Matches TeamUser.userId.username → TeamUser.userId.id
     */
    private fun resolvePlayerIds(): List<String> {
        return _selectedPlayers.value.mapNotNull { username ->
            _teamPlayers.value.firstOrNull { it.userId.username == username }?.userId?.id
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Space type mapping (iOS parity)
    // ═══════════════════════════════════════════════════════════

    companion object {
        fun spaceTypeToUI(backendValue: String?): String? = when (backendValue?.lowercase()) {
            "centre" -> "E-Sports Center"
            "virtual" -> "Virtual"
            else -> null
        }

        fun spaceTypeToBackend(uiValue: String?): String = when (uiValue?.lowercase()) {
            "e-sports center" -> "centre"
            "virtual" -> "virtual"
            else -> "centre"
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

        try {
            val date = dateFormatter.parse(event.startDate)
            date?.let { _initialSelectedDateMillis.value = it.time }
        } catch (_: Exception) {}

        // Match training player IDs to team usernames
        // Match training player IDs against team users to get usernames
        // iOS: teamPlayers.first(where: { $0.usersId?.id == playerId })?.usersId?.username
        _selectedPlayers.value = event.players?.mapNotNull { playerModel ->
            val playerId = playerModel.userId?.id ?: return@mapNotNull null
            _teamPlayers.value.firstOrNull { it.userId.id == playerId }?.userId?.username
        } ?: emptyList()

        if (event.type.lowercase() != "virtual") {
            val reserveSlotId = event.reserves?.firstOrNull()?.slot ?: return
            fetchAvailableSpaces {
                _selectedSpace.value = _availableSpaces.value.firstOrNull { space ->
                    space.slots.any { it.id == reserveSlotId }
                }
                val dayValue = calculateDayValue(_selectedDate.value ?: return@fetchAvailableSpaces)
                fetchAvailableSlots(dayValue) {
                    val reserveTimes = event.reserves?.firstOrNull()?.times
                        ?.mapNotNull { it.gamingSpaceTimesID?.time }?.toSet() ?: emptySet()
                    _selectedSlots.value = _availableSlots.value.filter { it.time in reserveTimes }
                    updateEnabledSlots()
                }
            }
        }
    }

    private fun populateFromIndividualReservation(reservation: Reservation) {
        _selectedDate.value = reservation.date

        try {
            val date = dateFormatter.parse(reservation.date)
            date?.let { _initialSelectedDateMillis.value = it.time }
        } catch (_: Exception) {}

        fetchAvailableSpaces {
            _selectedSpace.value = _availableSpaces.value.firstOrNull { space ->
                space.slots.any { it.id == reservation.slot.id }
            }
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

            val blockedDeferred = async { BlockedDaysService.fetchBlockedDates() }
            val individualDeferred = async { fetchIndividualReservations() }
            val teamDeferred = async { fetchTeamTrainings() }

            val blockedResult = blockedDeferred.await()
            val individualMarks = individualDeferred.await()
            val teamMarks = teamDeferred.await()

            val allMarks = mutableListOf<MarkedDate>()

            blockedResult.onSuccess { dates ->
                _blockedDates.value = dates
                allMarks.addAll(dates.map { MarkedDate(it, isBlocked = true) })
            }

            allMarks.addAll(individualMarks)
            allMarks.addAll(teamMarks)

            _markedDates.value = allMarks.distinctBy { it.date }
            calculateAvailableDates()
            _isLoading.value = false
        }
    }

    private suspend fun fetchIndividualReservations(): List<MarkedDate> {
        val userId = UserManager.getUser()?.id ?: return emptyList()
        return try {
            val result = ReservationApi.getReservations(userId)
            result.getOrDefault(emptyList()).mapNotNull { reservation ->
                reservation.date?.let { MarkedDate(it, isReservation = true) }
            }
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error fetching individual reservations: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchTeamTrainings(): List<MarkedDate> {
        val team = UserManager.getSelectedTeam() ?: return emptyList()
        return try {
            val result = TrainingApi.getTrainings(team.id)
            result.getOrDefault(emptyList()).mapNotNull { training ->
                MarkedDate(training.startDate, isTraining = true)
            }
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error fetching team trainings: ${e.message}")
            emptyList()
        }
    }

    private fun calculateAvailableDates() {
        val today = Calendar.getInstance()
        val dates = mutableListOf<String>()
        for (i in 0..30) {
            val formatted = dateFormatter.format(today.time)
            if (!_blockedDates.value.contains(formatted)) dates.add(formatted)
            today.add(Calendar.DAY_OF_YEAR, 1)
        }
        _availableDates.value = dates
    }

    // ═══════════════════════════════════════════════════════════
    // Date selection
    // ═══════════════════════════════════════════════════════════

    fun setSelectedDate(date: String) {
        // Check if blocked
        if (_markedDates.value.any { it.date == date && it.isBlocked }) return
        _selectedDate.value = date
        val dayValue = calculateDayValue(date)
        fetchAvailableSlots(dayValue)
    }

    private fun calculateDayValue(dateString: String): Int {
        val date = dateFormatter.parse(dateString) ?: return 0
        val calendar = Calendar.getInstance().apply { time = date }
        return calendar.get(Calendar.DAY_OF_WEEK) - 1
    }

    // ═══════════════════════════════════════════════════════════
    // Slots
    // ═══════════════════════════════════════════════════════════

    private fun fetchAvailableSlots(dayValue: Int, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = WeekTimeService.fetchWeekTimeByDay(dayValue)
            result.onSuccess { slots ->
                val isSimulador = _selectedSpace.value?.device?.lowercase()?.contains("simulador") == true
                _availableSlots.value = if (isSimulador) slots else slots.filter { !it.time.endsWith(":30") }
                updateEnabledSlots()
            }.onFailure { error ->
                Log.e("ReservationFlowVM", "Error fetching slots: ${error.message}")
            }
            onComplete?.invoke()
        }
    }

    private fun updateEnabledSlots() {
        val selected = _selectedSlots.value
        if (selected.isEmpty()) {
            _enabledSlots.value = _availableSlots.value
            return
        }

        val isSimulador = _selectedSpace.value?.device?.lowercase()?.contains("simulador") == true
        val maxSlots = if (isSimulador) 1 else if (personalReservations) 3 else 2

        val sorted = selected.sortedBy { it.value }
        val minVal = sorted.first().value
        val maxVal = sorted.last().value

        _enabledSlots.value = _availableSlots.value.filter { slot ->
            (slot.value in minVal..(maxVal + 1) && slot.value <= minVal + (maxSlots - 1))
                    || selected.any { it.id == slot.id }
        }
    }

    fun toggleSlotSelection(slot: GamingSpaceTime) {
        val current = _selectedSlots.value.toMutableList()
        if (current.any { it.id == slot.id }) current.removeAll { it.id == slot.id }
        else current.add(slot)
        _selectedSlots.value = current
        updateEnabledSlots()
    }

    fun cleanSlots() {
        _selectedSlots.value = emptyList()
        updateEnabledSlots()
    }

    // ═══════════════════════════════════════════════════════════
    // Spaces
    // ═══════════════════════════════════════════════════════════

    fun fetchAvailableSpaces(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = SpaceService.fetchSpaces()
            result.onSuccess { spaces ->
                _availableSpaces.value = if (!personalReservations) {
                    spaces.filter { !it.device.lowercase().contains("simulador") }
                } else spaces
            }.onFailure { error ->
                Log.e("ReservationFlowVM", "Error fetching spaces: ${error.message}")
            }
            onComplete?.invoke()
        }
    }

    fun selectSpace(space: Space) {
        if (_selectedSpace.value?.id == space.id) {
            _selectedSpace.value = null
        } else {
            _selectedSpace.value = space
            _availableSlots.value = emptyList()
            _selectedSlots.value = emptyList()
            _enabledSlots.value = emptyList()
        }
    }

    fun cleanSpace() {
        _selectedSpace.value = null
    }

    // ═══════════════════════════════════════════════════════════
    // Create individual reservation
    // ═══════════════════════════════════════════════════════════

    suspend fun createReservation(userId: String, withLogo: Bitmap?): Boolean {
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val slots = _selectedSlots.value
        if (slots.isEmpty()) return false

        val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }

        val reservation = ReservationWrapper(
            id = null, status = "active", slot = space.slots.first().id,
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
                        if (updateResult.isSuccess) { _reservationSuccess.value = true; true }
                        else { ReservationApi.deleteReservation(response.id); false }
                    } else { ReservationApi.deleteReservation(response.id); false }
                } else { response?.id?.let { ReservationApi.deleteReservation(it) }; false }
            } else false
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error creating reservation: ${e.message}"); false
        } finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update individual reservation (editing)
    // ═══════════════════════════════════════════════════════════

    suspend fun updateIndividualReservation(): Boolean {
        val reservationId = individualReservationInfo?.id ?: return false
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val slots = _selectedSlots.value
        if (slots.isEmpty()) return false

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
            _reservationSuccess.value = result.isSuccess
            result.isSuccess
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error updating individual: ${e.message}"); false
        } finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Create team training (centre)
    // ═══════════════════════════════════════════════════════════

    suspend fun createTeamReservation(withLogo: Bitmap?): Boolean {
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val teamId = UserManager.getSelectedTeam()?.id ?: return false
        val slots = _selectedSlots.value
        if (slots.isEmpty()) return false

        val playerIds = resolvePlayerIds()
        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
        val timeString = slots.sortedBy { it.value }.firstOrNull()?.time ?: _selectedTime.value
        val typeValue = spaceTypeToBackend(_selectedSpaceType.value)

        _isProcessing.value = true
        return try {
            // 1. Create training
            val trainingResult = TrainingApi.createTraining(
                TrainingRequest(
                    status = "active", type = typeValue, startDate = formattedDate,
                    time = timeString, team = teamId, notes = _reservationNotes.value,
                    players = playerIds.map { mapOf("users_id" to it) }, reserves = emptyList()
                )
            )

            val trainingId = trainingResult.getOrNull()?.id ?: return false

            // 2. Create reserve per player
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

                // Generate + upload QR
                if (reserveResponse.qrValue != null && reserveResponse.id != null) {
                    val qr = generateQrBitmap(reserveResponse.qrValue, logoBitmap = withLogo)
                    if (qr != null) {
                        val qrImage = UploadImageService().uploadQrBitmap(qr, "${reserveResponse.qrValue}.jpg")
                        val updated = reservation.copy(id = reserveResponse.id, qrImage = qrImage, qrValue = reserveResponse.qrValue)
                        ReservationApi.updateReservation(updated)
                    }
                }
            }

            if (reserveIds.isEmpty()) return false

            // 3. Link reserves to training
            TrainingApi.linkReservesToTraining(trainingId, reserveIds)
            _reservationSuccess.value = true
            true
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error creating team reservation: ${e.message}"); false
        } finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update team training (centre)
    // ═══════════════════════════════════════════════════════════

    suspend fun updateCenterTeamReservation(): Boolean {
        val trainingId = teamReservationInfo?.id ?: return false
        val reserveId = teamReservationInfo?.reserves?.firstOrNull()?.id ?: return false
        val date = _selectedDate.value ?: return false
        val space = _selectedSpace.value ?: return false
        val slots = _selectedSlots.value
        if (slots.isEmpty()) return false

        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
        val timeString = slots.sortedBy { it.value }.firstOrNull()?.time ?: _selectedTime.value
        val typeValue = spaceTypeToBackend(_selectedSpaceType.value)
        val playerIds = resolvePlayerIds()
        val mappedTimes = slots.map { mapOf("gaming_space_times_id" to GamingSpaceTimeId(id = it.id)) }

        _isProcessing.value = true
        return try {
            // Update reserve
            val reserveWrapper = ReservationWrapper(
                id = reserveId, status = "active", slot = space.slots.first().id,
                date = formattedDate, user = null, team = null, training = null,
                qrImage = null, qrValue = null, timesContainer = mappedTimes, peripheralLoans = null
            )
            ReservationApi.updateReservation(reserveWrapper)

            // Update training
            TrainingApi.updateTraining(trainingId, typeValue, formattedDate, timeString, _reservationNotes.value, playerIds)
            _reservationSuccess.value = true
            true
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error updating team centre: ${e.message}"); false
        } finally { _isProcessing.value = false }
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
            val result = TrainingApi.createTraining(
                TrainingRequest(
                    status = "active", type = "virtual", startDate = formattedDate,
                    time = _selectedTime.value, team = teamId, notes = _reservationNotes.value,
                    players = playerIds.map { mapOf("users_id" to it) }, reserves = emptyList()
                )
            )
            _reservationSuccess.value = result.isSuccess
            result.isSuccess
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error creating virtual: ${e.message}"); false
        } finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Update virtual team training
    // ═══════════════════════════════════════════════════════════

    suspend fun updateVirtualTeamReservation(): Boolean {
        val trainingId = teamReservationInfo?.id ?: return false
        val date = _selectedDate.value ?: return false
        if (_selectedTime.value.isEmpty()) return false

        val formattedDate = try { dateFormatter.format(dateFormatter.parse(date)!!) } catch (_: Exception) { return false }
        val playerIds = resolvePlayerIds()

        _isProcessing.value = true
        return try {
            val result = TrainingApi.updateTraining(
                trainingId, "virtual", formattedDate, _selectedTime.value, _reservationNotes.value, playerIds
            )
            _reservationSuccess.value = result.isSuccess
            result.isSuccess
        } catch (e: Exception) {
            Log.e("ReservationFlowVM", "Error updating virtual: ${e.message}"); false
        } finally { _isProcessing.value = false }
    }

    // ═══════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════

    data class MarkedDate(
        val date: String,
        val isReservation: Boolean = false,
        val isTraining: Boolean = false,
        val isBlocked: Boolean = false,
    )
}